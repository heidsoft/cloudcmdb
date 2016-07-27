package org.cmdbuild.logic.privileges;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.ATTRIBUTES_PRIVILEGES_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GRANT_CLASS_NAME;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GROUP_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_OBJECT_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGE_FILTER_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.STATUS_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.TYPE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.UI_CARD_EDIT_MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.PrivilegeMode.NONE;
import static org.cmdbuild.auth.privileges.constants.PrivilegeMode.READ;
import static org.cmdbuild.auth.privileges.constants.PrivilegeMode.WRITE;
import static org.cmdbuild.auth.privileges.constants.PrivilegeMode.of;
import static org.cmdbuild.auth.privileges.constants.PrivilegedObjectType.CLASS;
import static org.cmdbuild.auth.privileges.constants.PrivilegedObjectType.CUSTOMPAGE;
import static org.cmdbuild.auth.privileges.constants.PrivilegedObjectType.FILTER;
import static org.cmdbuild.auth.privileges.constants.PrivilegedObjectType.VIEW;
import static org.cmdbuild.common.Constants.ROLE_CLASS_NAME;
import static org.cmdbuild.dao.entrytype.Predicates.hasAnchestor;
import static org.cmdbuild.dao.entrytype.Predicates.isBaseClass;
import static org.cmdbuild.dao.entrytype.Predicates.isSystem;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.logic.privileges.PrivilegeInfo.EMPTY_ATTRIBUTES_PRIVILEGES;

import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.ForwardingSerializablePrivilege;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.privileges.constants.PrivilegedObjectType;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.custompages.CustomPage;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.logic.privileges.PrivilegeInfo.Builder;
import org.cmdbuild.model.profile.UIConfiguration;
import org.cmdbuild.model.view.View;
import org.cmdbuild.privileges.CustomPageAdapter;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.CustomPagePrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.store.filter.FilterStore;
import org.cmdbuild.services.store.filter.FilterStore.Filter;

import com.google.common.base.Optional;

public class DefaultSecurityLogic implements Logic, SecurityLogic {

	private final CMDataView view;
	private final CMClass grantClass;
	private final StorableConverter<View> viewConverter;
	private final FilterStore filterStore;
	private final CustomPagesLogic customPagesLogic;

	public DefaultSecurityLogic( //
			final CMDataView view, //
			final StorableConverter<View> viewConverter, //
			final FilterStore filterStore, //
			final CustomPagesLogic customPagesLogic //
	) {
		this.view = view;
		this.grantClass = view.findClass(GRANT_CLASS_NAME);
		this.viewConverter = viewConverter;
		this.filterStore = filterStore;
		this.customPagesLogic = customPagesLogic;
	}

	@Override
	public List<PrivilegeInfo> fetchClassPrivilegesForGroup(final Long groupId) {
		return fetchClassPrivilegesForGroup(groupId, allButNonProcessClasses());
	}

	private Iterable<CMClass> allButNonProcessClasses() {
		return from(view.findClasses()) //
				.filter(CMClass.class) //
				.filter(not(or(isSystem(equalTo(true)), isBaseClass(equalTo(true)),
						hasAnchestor(view.getActivityClass()))));
	}

	@Override
	public List<PrivilegeInfo> fetchProcessPrivilegesForGroup(final Long groupId) {
		return fetchClassPrivilegesForGroup(groupId, processClasses());
	}

	private Iterable<CMClass> processClasses() {
		return from(view.findClasses()) //
				.filter(CMClass.class) //
				.filter(not(or(isSystem(equalTo(true)), isBaseClass(equalTo(true)),
						not(hasAnchestor(view.getActivityClass())))));
	}

	private List<PrivilegeInfo> fetchClassPrivilegesForGroup(final Long groupId, final Iterable<CMClass> classes) {
		final List<PrivilegeInfo> output = newArrayList();
		final List<PrivilegeInfo> stored = fetchStoredPrivilegesForGroup(groupId, CLASS);
		for (final CMClass clazz : classes) {
			final Long classId = clazz.getId();
			final PrivilegeInfo element = getPrivilegedElement(stored, classId);
			if (element == null) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, clazz, NONE, null);

				final List<String> attributesPrivileges = newArrayList();
				for (final CMAttribute attribute : clazz.getAttributes()) {
					final String mode = attribute.getMode().name().toLowerCase();
					attributesPrivileges.add(String.format("%s:%s", attribute.getName(), mode));
				}

				pi.setAttributesPrivileges(attributesPrivileges.toArray(new String[attributesPrivileges.size()]));

				output.add(pi);
			} else {
				output.add(element);
			}
		}
		return output;
	}

	@Override
	public CardEditMode fetchCardEditModeForGroupAndClass(final Long groupId, final Long classId) {
		final List<PrivilegeInfo> fetchClassPrivilegesForGroup = fetchClassPrivilegesForGroup(groupId);
		CardEditMode cardEditMode = null;
		for (final PrivilegeInfo privilegeInfo : fetchClassPrivilegesForGroup) {
			if (privilegeInfo.getPrivilegedObjectId().equals(classId)) {
				cardEditMode = privilegeInfo.getCardEditMode();
				break;
			}
		}
		cardEditMode = defaultIfNull(cardEditMode, CardEditMode.ALLOW_ALL);
		return cardEditMode;
	}

	@Override
	public List<PrivilegeInfo> fetchViewPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedViewPrivileges = fetchStoredPrivilegesForGroup(groupId, VIEW);
		final Iterable<View> allViews = fetchAllViews();
		for (final View view : allViews) {
			final Long viewId = view.getId();
			if (!isPrivilegeAlreadyStored(viewId, fetchedViewPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, view, NONE, null);
				fetchedViewPrivileges.add(pi);
			}
		}
		return fetchedViewPrivileges;
	}

	@Override
	public List<PrivilegeInfo> fetchFilterPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> fetchedFilterPrivileges = fetchStoredPrivilegesForGroup(groupId, FILTER);
		final Iterable<Filter> allGroupsFilters = filterStore.readSharedFilters(null, 0, MAX_VALUE);
		for (final Filter filter : allGroupsFilters) {
			final Long filterId = Long.valueOf(filter.getId());
			if (!isPrivilegeAlreadyStored(filterId, fetchedFilterPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, filter, NONE, null);
				fetchedFilterPrivileges.add(pi);
			}
		}
		return fetchedFilterPrivileges;
	}

	@Override
	public List<PrivilegeInfo> fetchCustomViewPrivilegesForGroup(final Long groupId) {
		final List<PrivilegeInfo> alreadyStoredPrivileges = fetchStoredPrivilegesForGroup(groupId, CUSTOMPAGE);
		for (final CustomPage element : customPagesLogic.read()) {
			if (!isPrivilegeAlreadyStored(element.getId(), alreadyStoredPrivileges)) {
				final PrivilegeInfo pi = new PrivilegeInfo(groupId, new CustomPageAdapter(element), NONE, null);
				alreadyStoredPrivileges.add(pi);
			}
		}
		return alreadyStoredPrivileges;
	}

	private Iterable<View> fetchAllViews() {
		// TODO must be an external dependency
		final DataViewStore<View> viewStore = DataViewStore.newInstance(view, viewConverter);
		return viewStore.readAll();
	}

	/**
	 * Fetches the privileges for specified group. NOTE that the group has no
	 * privilege if it is retrieved and fetched as 'none' or if it is not stored
	 * in the database
	 */
	private List<PrivilegeInfo> fetchStoredPrivilegesForGroup(final Long groupId, final PrivilegedObjectType type) {
		final PrivilegeFetcherFactory privilegeFetcherFactory = getPrivilegeFetcherFactoryForType(type);
		privilegeFetcherFactory.setGroupId(groupId);
		final PrivilegeFetcher privilegeFetcher = privilegeFetcherFactory.create();
		final Iterable<PrivilegePair> privilegePairs = privilegeFetcher.fetch();
		return fromPrivilegePairToPrivilegeInfo(privilegePairs, groupId);
	}

	/**
	 * TODO: use a visitor instead to be sure to consider all cases
	 */
	private PrivilegeFetcherFactory getPrivilegeFetcherFactoryForType(final PrivilegedObjectType type) {
		switch (type) {
		case VIEW:
			// TODO must me an external dependency
			return new ViewPrivilegeFetcherFactory(view, viewConverter);
		case CLASS:
			return new CMClassPrivilegeFetcherFactory(view);
		case FILTER:
			return new FilterPrivilegeFetcherFactory(view, filterStore);
		case CUSTOMPAGE:
			return new CustomPagePrivilegeFetcherFactory(view, customPagesLogic);
		default:
			return null;
		}
	}

	private List<PrivilegeInfo> fromPrivilegePairToPrivilegeInfo(final Iterable<PrivilegePair> privilegePairs,
			final Long groupId) {
		final List<PrivilegeInfo> list = newArrayList();
		for (final PrivilegePair privilegePair : privilegePairs) {
			final SerializablePrivilege privilegedObject = privilegePair.privilegedObject;
			final CMPrivilege privilege = privilegePair.privilege;
			PrivilegeInfo privilegeInfo;
			if (privilege.implies(DefaultPrivileges.WRITE)) {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, WRITE, null);
			} else if (privilege.implies(DefaultPrivileges.READ)) {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, READ, null);
			} else {
				privilegeInfo = new PrivilegeInfo(groupId, privilegedObject, NONE, null);
			}
			privilegeInfo.setPrivilegeFilter(privilegePair.privilegeFilter);
			privilegeInfo.setAttributesPrivileges(privilegePair.attributesPrivileges);
			privilegeInfo.setCardEditMode(CardEditMode.PERSISTENCE_TO_LOGIC.apply(privilegePair.cardEditMode));
			list.add(privilegeInfo);
		}
		return list;
	}

	private boolean isPrivilegeAlreadyStored(final Long privilegedObjectId, final Iterable<PrivilegeInfo> elements) {
		return getPrivilegedElement(elements, privilegedObjectId) != null;
	}

	private PrivilegeInfo getPrivilegedElement(final Iterable<PrivilegeInfo> elements, final Long privilegedObjectId) {
		for (final PrivilegeInfo element : elements) {
			final Long value = element.getPrivilegedObjectId();
			if (value != null && value.equals(privilegedObjectId)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * FIXME
	 * 
	 * this methods is called for two different purposes
	 * 
	 * 1) change class-mode
	 * 
	 * 2) change row and column privileges configuration
	 * 
	 * Remove the mode only flag and implement two different methods or uniform
	 * the values set in the privilegeInfo object in order to have all the
	 * attributes and update them all
	 */
	@Override
	public void saveClassPrivilege(final PrivilegeInfo privilegeInfo, final boolean modeOnly) {
		final Optional<CMCard> _grantCard = getPrivilegeCard(privilegeInfo);

		if (_grantCard.isPresent()) {
			final CMCard grantCard = _grantCard.get();
			final Long entryTypeId = grantCard.get(PRIVILEGED_CLASS_ID_ATTRIBUTE, Long.class);
			if (entryTypeId.equals(privilegeInfo.getPrivilegedObjectId())) {
				if (modeOnly) {
					// replace the privilegeInfo with the
					// data already stored to not override them
					final Object filter = grantCard.get(PRIVILEGE_FILTER_ATTRIBUTE);
					if (filter != null) {
						privilegeInfo.setPrivilegeFilter((String) filter);
					}

					final Object attributes = grantCard.get(ATTRIBUTES_PRIVILEGES_ATTRIBUTE);
					if (attributes != null) {
						privilegeInfo.setAttributesPrivileges((String[]) attributes);
					}
				} else {
					if (privilegeInfo.getPrivilegeFilter() == null) {
						privilegeInfo.setPrivilegeFilter(grantCard.get(PRIVILEGE_FILTER_ATTRIBUTE, String.class));
					}

					if (privilegeInfo.getAttributesPrivileges() == null) {
						privilegeInfo.setAttributesPrivileges(
								grantCard.get(ATTRIBUTES_PRIVILEGES_ATTRIBUTE, String[].class));
					} else {
						/*
						 * Iterate over the attributes privileges and keep only
						 * the ones that override the mode of the attribute
						 */
						final CMEntryType entryType = view.findClass(entryTypeId);
						final Map<String, String> attributeModes = attributesMode(entryType);
						final List<String> attributesPrivilegesToSave = newArrayList();
						for (final String attributePrivilege : privilegeInfo.getAttributesPrivileges()) {
							final String[] parts = attributePrivilege.split(":");
							final String attributeName = parts[0];
							final String privilege = parts[1];
							if (attributeModes.containsKey(attributeName)) {
								if (!attributeModes.get(attributeName).equals(privilege)) {
									attributesPrivilegesToSave.add(attributePrivilege);
								}
							}
						}
						privilegeInfo.setAttributesPrivileges( //
								attributesPrivilegesToSave.toArray( //
										new String[attributesPrivilegesToSave.size()] //
						));
					}
				}
				privilegeInfo.setCardEditMode(
						CardEditMode.PERSISTENCE_TO_LOGIC.apply((String) grantCard.get(UI_CARD_EDIT_MODE_ATTRIBUTE)));
				updateGrantCard(grantCard, privilegeInfo);
			}
		} else {
			createClassGrantCard(privilegeInfo);
		}
	}

	private Map<String, String> attributesMode(final CMEntryType entryType) {
		final Map<String, String> privileges = newHashMap();
		for (final CMAttribute attribute : entryType.getActiveAttributes()) {
			if (attribute.isActive()) {
				final String mode;
				switch (attribute.getMode()) {
				case HIDDEN:
					mode = "none";
					break;
				default:
					mode = attribute.getMode().name().toLowerCase();
					break;
				}
				privileges.put(attribute.getName(), mode);
			}
		}

		return privileges;
	}

	@Override
	public void saveProcessPrivilege(final PrivilegeInfo privilegeInfo, final boolean modeOnly) {
		saveClassPrivilege(privilegeInfo, modeOnly);
	}

	@Override
	public void saveViewPrivilege(final PrivilegeInfo privilegeInfo) {
		final CMQueryResult result = view.select(anyAttribute(grantClass)).from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(privilegeInfo.getGroupId())),
						condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(VIEW.getValue())))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final Long storedViewId = ((Integer) grantCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE)).longValue();
			if (storedViewId.equals(privilegeInfo.getPrivilegedObjectId())) {
				updateGrantCard(grantCard, privilegeInfo);
				return;
			}
		}

		createViewGrantCard(privilegeInfo);
	}

	@Override
	public void saveFilterPrivilege(final PrivilegeInfo privilegeInfo) {
		final CMQueryResult result = view.select(anyAttribute(grantClass)).from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(privilegeInfo.getGroupId())),
						condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(FILTER.getValue())))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final Long storedViewId = ((Integer) grantCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE)).longValue();
			if (storedViewId.equals(privilegeInfo.getPrivilegedObjectId())) {
				updateGrantCard(grantCard, privilegeInfo);
				return;
			}
		}

		createFilterGrantCard(privilegeInfo);
	}

	@Override
	public void saveCustomPagePrivilege(final PrivilegeInfo privilegeInfo) {
		final CMQueryResult result = view.select(anyAttribute(grantClass)).from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(privilegeInfo.getGroupId())),
						condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(CUSTOMPAGE.getValue())))) //
				.run();

		for (final CMQueryRow row : result) {
			final CMCard grantCard = row.getCard(grantClass);
			final Long storedViewId = grantCard.get(PRIVILEGED_OBJECT_ID_ATTRIBUTE, Integer.class).longValue();
			if (storedViewId.equals(privilegeInfo.getPrivilegedObjectId())) {
				updateGrantCard(grantCard, privilegeInfo);
				return;
			}
		}

		createCustomPageGrantCard(privilegeInfo);

	}

	private void updateGrantCard(final CMCard grantCard, final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition mutableGrantCard = view.update(grantCard);
		if (privilegeInfo.getMode() != null) {
			// check if null to allow the update of other attributes
			// without specify the mode
			mutableGrantCard.set(MODE_ATTRIBUTE, privilegeInfo.getMode().getValue()); //
		}

		final String persistenceCardEditMode = CardEditMode.LOGIC_TO_PERSISTENCE.apply(privilegeInfo.getCardEditMode());

		mutableGrantCard //
				.set(PRIVILEGE_FILTER_ATTRIBUTE, privilegeInfo.getPrivilegeFilter()) //
				.set(ATTRIBUTES_PRIVILEGES_ATTRIBUTE,
						defaultIfNull(privilegeInfo.getAttributesPrivileges(), EMPTY_ATTRIBUTES_PRIVILEGES)) //
				.set(UI_CARD_EDIT_MODE_ATTRIBUTE, persistenceCardEditMode) //
				.save();
	}

	private void createClassGrantCard(final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);

		// manage the null value for the privilege mode
		// it could happen updating row and column privileges
		final PrivilegeMode privilegeMode = defaultIfNull(privilegeInfo.getMode(), NONE);

		final String persistenceCardEditMode = CardEditMode.LOGIC_TO_PERSISTENCE.apply(privilegeInfo.getCardEditMode());

		grantCardToBeCreated //
				.set(GROUP_ID_ATTRIBUTE, privilegeInfo.getGroupId()) //
				.set(PRIVILEGED_CLASS_ID_ATTRIBUTE, privilegeInfo.getPrivilegedObjectId()) //
				.set(MODE_ATTRIBUTE, privilegeMode.getValue()) //
				.set(TYPE_ATTRIBUTE, CLASS.getValue()) //
				.set(PRIVILEGE_FILTER_ATTRIBUTE, privilegeInfo.getPrivilegeFilter()) //
				.set(ATTRIBUTES_PRIVILEGES_ATTRIBUTE,
						defaultIfNull(privilegeInfo.getAttributesPrivileges(), EMPTY_ATTRIBUTES_PRIVILEGES)) //
				.set(STATUS_ATTRIBUTE, CardStatus.ACTIVE.value()) //
				.set(UI_CARD_EDIT_MODE_ATTRIBUTE, persistenceCardEditMode) //
				.save();
	}

	private void createViewGrantCard(final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);
		grantCardToBeCreated.set(GROUP_ID_ATTRIBUTE, privilegeInfo.getGroupId()) //
				.set(PRIVILEGED_OBJECT_ID_ATTRIBUTE, privilegeInfo.getPrivilegedObjectId()) //
				.set(MODE_ATTRIBUTE, privilegeInfo.getMode().getValue()) //
				.set(TYPE_ATTRIBUTE, VIEW.getValue()) //
				.set(STATUS_ATTRIBUTE, CardStatus.ACTIVE.value()) //
				.save();
	}

	private void createFilterGrantCard(final PrivilegeInfo privilegeInfo) {
		final CMCardDefinition grantCardToBeCreated = view.createCardFor(grantClass);
		grantCardToBeCreated.set(GROUP_ID_ATTRIBUTE, privilegeInfo.getGroupId()) //
				.set(PRIVILEGED_OBJECT_ID_ATTRIBUTE, privilegeInfo.getPrivilegedObjectId()) //
				.set(MODE_ATTRIBUTE, privilegeInfo.getMode().getValue()) //
				.set(TYPE_ATTRIBUTE, FILTER.getValue()) //
				.set(STATUS_ATTRIBUTE, CardStatus.ACTIVE.value()) //
				.save();
	}

	private void createCustomPageGrantCard(final PrivilegeInfo privilegeInfo) {
		view.createCardFor(grantClass) //
				.set(GROUP_ID_ATTRIBUTE, privilegeInfo.getGroupId()) //
				.set(PRIVILEGED_OBJECT_ID_ATTRIBUTE, privilegeInfo.getPrivilegedObjectId()) //
				.set(MODE_ATTRIBUTE, privilegeInfo.getMode().getValue()) //
				.set(TYPE_ATTRIBUTE, CUSTOMPAGE.getValue()) //
				.set(STATUS_ATTRIBUTE, CardStatus.ACTIVE.value()) //
				.save();
	}

	@Override
	public UIConfiguration fetchGroupUIConfiguration(final Long groupId) {
		final CMClass roleClass = view.findClass(ROLE_CLASS_NAME);
		final CMQueryRow row = view.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Id"), eq(groupId))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();
		final CMCard roleCard = row.getCard(roleClass);
		final UIConfiguration uiConfiguration = new UIConfiguration();

		final String[] disabledModules = (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDMODULES);
		if (!isStringArrayNull(disabledModules)) {
			uiConfiguration.setDisabledModules(disabledModules);
		}

		final String[] disabledCardTabs = (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDCARDTABS);
		if (!isStringArrayNull(disabledCardTabs)) {
			uiConfiguration.setDisabledCardTabs(disabledCardTabs);
		}

		final String[] disabledProcessTabs = (String[]) roleCard.get(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS);
		if (!isStringArrayNull(disabledProcessTabs)) {
			uiConfiguration.setDisabledProcessTabs(disabledProcessTabs);
		}
		uiConfiguration.setHideSidePanel((Boolean) roleCard.get(GROUP_ATTRIBUTE_HIDESIDEPANEL));
		uiConfiguration.setFullScreenMode((Boolean) roleCard.get(GROUP_ATTRIBUTE_FULLSCREEN));
		uiConfiguration.setSimpleHistoryModeForCard((Boolean) roleCard.get(GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD));
		uiConfiguration.setSimpleHistoryModeForProcess((Boolean) roleCard.get(GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS));
		uiConfiguration
				.setProcessWidgetAlwaysEnabled((Boolean) roleCard.get(GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED));
		uiConfiguration.setCloudAdmin((Boolean) roleCard.get(GROUP_ATTRIBUTE_CLOUD_ADMIN));

		return uiConfiguration;
	}

	private boolean isStringArrayNull(final String[] stringArray) {
		if (stringArray == null) {
			return true;
		} else if (stringArray.length == 0) {
			return true;
		} else if (stringArray.length == 1 && stringArray[0] == null) {
			return true;
		}
		return false;
	}

	@Override
	public void saveGroupUIConfiguration(final Long groupId, final UIConfiguration configuration) {
		final CMClass roleClass = view.findClass(ROLE_CLASS_NAME);
		final CMQueryRow row = view.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Id"), eq(groupId))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();
		final CMCard roleCard = row.getCard(roleClass);
		final CMCardDefinition cardDefinition = view.update(roleCard);
		if (isStringArrayNull(configuration.getDisabledModules())) {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDMODULES, null);
		} else {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDMODULES, configuration.getDisabledModules());
		}
		if (isStringArrayNull(configuration.getDisabledCardTabs())) {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDCARDTABS, null);
		} else {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDCARDTABS, configuration.getDisabledCardTabs());
		}
		if (isStringArrayNull(configuration.getDisabledProcessTabs())) {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS, null);
		} else {
			cardDefinition.set(GROUP_ATTRIBUTE_DISABLEDPROCESSTABS, configuration.getDisabledProcessTabs());
		}
		cardDefinition.set(GROUP_ATTRIBUTE_HIDESIDEPANEL, configuration.isHideSidePanel());
		cardDefinition.set(GROUP_ATTRIBUTE_FULLSCREEN, configuration.isFullScreenMode());
		cardDefinition.set(GROUP_ATTRIBUTE_SIMPLE_HISTORY_CARD, configuration.isSimpleHistoryModeForCard());
		cardDefinition.set(GROUP_ATTRIBUTE_SIMPLE_HISTORY_PROCESS, configuration.isSimpleHistoryModeForProcess());
		cardDefinition.set(GROUP_ATTRIBUTE_PROCESS_WIDGET_ALWAYS_ENABLED, configuration.isProcessWidgetAlwaysEnabled());
		// FIXME: manage cloud admin
		cardDefinition.save();
	}

	@Override
	public void saveCardEditMode(final PrivilegeInfo privilegeInfoToSave) {

		final Optional<CMCard> _privilegeCard = getPrivilegeCard(privilegeInfoToSave);

		if (!_privilegeCard.isPresent()) {
			createClassGrantCard(privilegeInfoToSave);
		} else {
			final CMCard privilegeCard = _privilegeCard.get();
			final PrivilegeMode classMode = of(privilegeCard.get(MODE_ATTRIBUTE));
			final Object attributesPrivileges = privilegeCard.get(ATTRIBUTES_PRIVILEGES_ATTRIBUTE);
			final Object privilegeFilter = privilegeCard.get(PRIVILEGE_FILTER_ATTRIBUTE);
			final SerializablePrivilege privilegeObject = privilegeObjectFromId(
					privilegeInfoToSave.getPrivilegedObjectId());

			Builder privilegeBuilder = PrivilegeInfo.newInstance() //
					.withGroupId(privilegeInfoToSave.getGroupId()) //
					.withPrivilegedObject(privilegeObject) //
					.withPrivilegeMode(classMode) //
					.withCardEditMode(privilegeInfoToSave.getCardEditMode());
			if (attributesPrivileges != null) {
				privilegeBuilder = privilegeBuilder.withAttributesPrivileges((String[]) attributesPrivileges);
			}
			if (privilegeFilter != null) {
				privilegeBuilder = privilegeBuilder.withPrivilegeFilter((String) privilegeFilter);
			}
			final PrivilegeInfo privilegeToUpdate = privilegeBuilder.build();
			updateGrantCard(privilegeCard, privilegeToUpdate);
		}

	}

	private Optional<CMCard> getPrivilegeCard(final PrivilegeInfo privilegeInfoToSave) {
		final CMQueryResult rowsForGroupAndClass = view.select(anyAttribute(grantClass)).from(grantClass)
				.where( //
						and( //
								condition(attribute(grantClass, GROUP_ID_ATTRIBUTE),
										eq(privilegeInfoToSave.getGroupId())), //
								condition(attribute(grantClass, TYPE_ATTRIBUTE), eq(CLASS.getValue())), //
								condition(attribute(grantClass, PRIVILEGED_CLASS_ID_ATTRIBUTE),
										eq(privilegeInfoToSave.getPrivilegedObjectId()))) //
		) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run();
		Optional<CMCard> optional;
		if (!rowsForGroupAndClass.isEmpty()) {
			final CMCard privilegeCard = rowsForGroupAndClass.getOnlyRow().getCard(grantClass);
			optional = Optional.of(privilegeCard);
		} else {
			optional = Optional.absent();
		}
		return optional;
	}

	private static final SerializablePrivilege privilegeObjectFromId(final Long id) {
		final SerializablePrivilege unsupported = UnsupportedProxyFactory.of(SerializablePrivilege.class).create();
		return new ForwardingSerializablePrivilege() {

			@Override
			protected SerializablePrivilege delegate() {
				return unsupported;
			}

			@Override
			public Long getId() {
				return id;
			}

		};
	}
}
