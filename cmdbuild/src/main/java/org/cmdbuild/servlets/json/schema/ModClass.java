package org.cmdbuild.servlets.json.schema;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Collections.emptyList;
import static org.cmdbuild.dao.entrytype.Predicates.isSystem;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_VALUE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DISABLED1;
import static org.cmdbuild.servlets.json.CommunicationConstants.DISABLED2;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAINS;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_CARDINALITY;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_FIRST_CLASS_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_IS_MASTER_DETAIL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_MASTER_DETAIL_LABEL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_SECOND_CLASS_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.EDITOR_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD_MODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.FK_DESTINATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FORCE_CREATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.INDEX;
import static org.cmdbuild.servlets.json.CommunicationConstants.INHERIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.IP_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.IS_PROCESS;
import static org.cmdbuild.servlets.json.CommunicationConstants.LENGTH;
import static org.cmdbuild.servlets.json.CommunicationConstants.LOOKUP;
import static org.cmdbuild.servlets.json.CommunicationConstants.META;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOT_NULL;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRECISION;
import static org.cmdbuild.servlets.json.CommunicationConstants.SCALE;
import static org.cmdbuild.servlets.json.CommunicationConstants.SHOW_IN_GRID;
import static org.cmdbuild.servlets.json.CommunicationConstants.SKIP_DISABLED_CLASSES;
import static org.cmdbuild.servlets.json.CommunicationConstants.SUPERCLASS;
import static org.cmdbuild.servlets.json.CommunicationConstants.TABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TABLE_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPES;
import static org.cmdbuild.servlets.json.CommunicationConstants.UNIQUE;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER_STOPPABLE;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMTableType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic.FunctionItem;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.IpType;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer.JsonModeMapper;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class ModClass extends JSONBaseWithSpringContext {

	private static final AttributesQuery UNUSED_ATTRIBUTE_QUERY = new AttributesQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	private static final Iterable<String> NO_DISABLED = emptyList();

	private static class JsonFunctionItem implements FunctionItem {

		private final FunctionItem delegate;

		public JsonFunctionItem(final FunctionItem delegate) {
			this.delegate = delegate;
		}

		@Override
		@JsonProperty(NAME)
		public String name() {
			return delegate.name();
		}

	}

	private static Function<FunctionItem, FunctionItem> toJsonFunction = new Function<FunctionItem, FunctionItem>() {

		@Override
		public FunctionItem apply(final FunctionItem input) {
			return new JsonFunctionItem(input);
		}

	};

	@JSONExported
	public JSONObject getAllClasses( //
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException, AuthException, CMWorkflowException {

		final Iterable<? extends CMClass> classesToBeReturned = userDataAccessLogic().findClasses(activeOnly);
		final Iterable<? extends UserProcessClass> processClasses = workflowLogic().findProcessClasses(activeOnly);

		final JSONArray serializedClasses = new JSONArray();
		for (final CMClass cmClass : classesToBeReturned) {
			final JSONObject classObject = classSerializer().toClient(cmClass);
			Serializer.addAttachmentsData(classObject, cmClass, dmsLogic(), notifier());
			serializedClasses.put(classObject);
		}

		for (final UserProcessClass userProcessClass : processClasses) {
			final JSONObject classObject = classSerializer().toClient(userProcessClass, activeOnly);
			Serializer.addAttachmentsData(classObject, userProcessClass, dmsLogic(), notifier());
			serializedClasses.put(classObject);

			// do this check only for the request
			// of active classes AKA the management module
			if (activeOnly) {
				try {
					alertAdminIfNoStartActivity(userProcessClass);
				} catch (final Exception ex) {
					logger.error(String.format("Error retrieving start activity for process",
							userProcessClass.getName()));
				}
			}
		}

		return new JSONObject() {
			{
				put("classes", serializedClasses);
			}
		};
	}

	/**
	 * @param element
	 * @throws CMWorkflowException
	 */
	private void alertAdminIfNoStartActivity(final UserProcessClass element) throws CMWorkflowException {
		try {
			workflowLogic().getStartActivityOrDie(element.getName());
		} catch (final CMDBWorkflowException ex) {
			// throw an exception to say to the user
			// that the XPDL has no adminStart
			if (WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.equals(ex.getExceptionType())
					&& !element.isSuperclass() && userStore().getUser().hasAdministratorPrivileges()) {
				notifier().warn(ex);
			} else {
				throw ex;
			}
		}
	}

	@JSONExported
	public JSONObject saveTable( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(value = INHERIT, required = false) final int idParent, //
			@Parameter(value = SUPERCLASS, required = false) final boolean isSuperClass, //
			@Parameter(value = IS_PROCESS, required = false) final boolean isProcess, //
			@Parameter(value = TABLE_TYPE, required = false) String tableType, //
			@Parameter(ACTIVE) final boolean isActive, //
			@Parameter(USER_STOPPABLE) final boolean isProcessUserStoppable, //
			@Parameter(FORCE_CREATION) final boolean forceCreation) throws JSONException, CMDBException {

		if (tableType == "") {
			tableType = EntryType.TableType.standard.name();
		}

		final EntryType entryType = EntryType.newClass() //
				.withTableType(EntryType.TableType.valueOf(tableType)).withName(name) //
				.withDescription(description) //
				.withParent(Long.valueOf(idParent)) //
				.thatIsSuperClass(isSuperClass) //
				.thatIsProcess(isProcess) //
				.thatIsUserStoppable(isProcessUserStoppable) //
				.thatIsActive(isActive) //
				.thatIsSystem(false) //
				.build();

		final CMClass cmClass = dataDefinitionLogic().createOrUpdate(entryType, forceCreation);
		return classSerializer().toClient(cmClass, TABLE);
	}

	@JSONExported
	public void deleteTable(@Parameter(value = CLASS_NAME) final String className) throws JSONException, CMDBException {
		dataDefinitionLogic().deleteOrDeactivate(className);
	}

	@JSONExported
	public JSONObject getAttributeList( //
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly, //
			@Parameter(value = CLASS_NAME) final String className //
	) throws JSONException, AuthException {
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final Iterable<? extends CMAttribute> attributes = dataLogic.getAttributes(className, activeOnly,
				UNUSED_ATTRIBUTE_QUERY);

		final AttributeSerializer attributeSerializer = AttributeSerializer.newInstance() //
				.withDataView(systemDataView()) //
				.build();
		final JSONObject out = new JSONObject();
		out.put(ATTRIBUTES, attributeSerializer.toClient(attributes, activeOnly));
		return out;
	}

	@JSONExported
	public void saveOrderCriteria(@Parameter(value = ATTRIBUTES) final JSONObject orderCriteria, //
			@Parameter(value = CLASS_NAME) final String className) throws Exception {

		final List<ClassOrder> classOrders = Lists.newArrayList();
		final Iterator<?> keysIterator = orderCriteria.keys();
		while (keysIterator.hasNext()) {
			final String key = (String) keysIterator.next();
			classOrders.add(ClassOrder.from(key, orderCriteria.getInt(key)));
		}
		dataDefinitionLogic().changeClassOrders(className, classOrders);
	}

	/**
	 * 
	 * @param tableTypeStirng
	 *            can be CLASS or SIMPLECLASS
	 * @return a list of attribute types that a class or superclass can have.
	 * @throws JSONException
	 * @throws AuthException
	 */
	@JSONExported
	public JSONObject getAttributeTypes( //
			@Parameter(TABLE_TYPE) final String tableTypeStirng) //
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final CMTableType tableType = CMTableType.valueOf(tableTypeStirng);
		final List<CMAttributeType<?>> types = new LinkedList<CMAttributeType<?>>();
		for (final CMAttributeType<?> type : tableType.getAvaiableAttributeList()) {
			types.add(type);
		}
		out.put(TYPES, AttributeSerializer.toClient(types));
		return out;
	}

	@Admin
	@JSONExported
	public JSONObject saveAttribute( //
			final JSONObject serializer, //
			@Parameter(value = NAME, required = false) final String name, //
			@Parameter(value = TYPE, required = false) final String attributeTypeString, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(value = DEFAULT_VALUE, required = false) final String defaultValue, //
			@Parameter(SHOW_IN_GRID) final boolean isBaseDSP, //
			@Parameter(NOT_NULL) final boolean isNotNull, //
			@Parameter(UNIQUE) final boolean isUnique, //
			@Parameter(ACTIVE) final boolean isActive, //
			@Parameter(FIELD_MODE) final String fieldMode, //
			@Parameter(value = LENGTH, required = false) final int length, //
			@Parameter(value = PRECISION, required = false) final int precision, //
			@Parameter(value = SCALE, required = false) final int scale, //
			@Parameter(value = LOOKUP, required = false) final String lookupType, //
			@Parameter(value = DOMAIN_NAME, required = false) final String domainName, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = FK_DESTINATION, required = false) final String fkDestinationName, //
			@Parameter(value = GROUP, required = false) final String group, //
			@Parameter(value = META, required = false) final JSONObject meta, //
			@Parameter(value = EDITOR_TYPE, required = false) final String editorType, //
			@Parameter(value = IP_TYPE, required = false) final String ipType, //
			@Parameter(value = CLASS_NAME) final String className //
	) throws Exception {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(name) //
				.withOwnerName(className) //
				.withDescription(description) //
				.withGroup(group) //
				.withType(attributeTypeString) //
				.withLength(length) //
				.withPrecision(precision) //
				.withScale(scale) //
				.withLookupType(lookupType) //
				.withDomain(domainName) //
				.withDefaultValue(defaultValue) //
				.withMode(JsonModeMapper.modeFrom(fieldMode)) //
				.withEditorType(editorType) //
				.withFilter(filter) //
				.withIpType(IpType.of(ipType)) //
				.withForeignKeyDestinationClassName(fkDestinationName) //
				.thatIsDisplayableInList(isBaseDSP) //
				.thatIsMandatory(isNotNull) //
				.thatIsUnique(isUnique) //
				.thatIsActive(isActive) //
				.withMetadata(toMap(meta)) //
				.build();
		final DataDefinitionLogic logic = dataDefinitionLogic();
		final CMAttribute cmAttribute = logic.createOrUpdate(attribute);

		final AttributeSerializer attributeSerializer = AttributeSerializer.newInstance() //
				.withDataView(logic.getView()) //
				.build();
		final JSONObject result = attributeSerializer.toClient(cmAttribute, attribute.getMetadata());
		serializer.put(ATTRIBUTE, result);
		return serializer;
	}

	@JSONExported
	public void deleteAttribute( //
			@Parameter(NAME) final String attributeName, //
			@Parameter(CLASS_NAME) final String className) {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(attributeName) //
				.withOwnerName(className) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(attribute);
	}

	@JSONExported
	public void reorderAttribute( //
			@Parameter(ATTRIBUTES) final String jsonAttributeList, //
			@Parameter(CLASS_NAME) final String className //
	) throws Exception {
		final List<Attribute> attributes = Lists.newArrayList();
		final JSONArray jsonAttributes = new JSONArray(jsonAttributeList);
		for (int i = 0; i < jsonAttributes.length(); i++) {
			final JSONObject jsonAttribute = jsonAttributes.getJSONObject(i);
			attributes.add(Attribute.newAttribute().withOwnerName(className)//
					.withName(jsonAttribute.getString(NAME)) //
					.withIndex(jsonAttribute.getInt(INDEX)).build());
		}

		for (final Attribute attribute : attributes) {
			dataDefinitionLogic().reorder(attribute);
		}
	}

	@JSONExported
	public JSONObject getAllDomains( //
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException, AuthException {

		final Iterable<? extends CMDomain> almostAllDomains;
		if (activeOnly) {
			almostAllDomains = from(userDataAccessLogic().findActiveDomains()) //
					.filter(domainsWithActiveClasses());
		} else {
			almostAllDomains = userDataAccessLogic().findAllDomains();
		}
		final Iterable<? extends CMDomain> domains = from(almostAllDomains) //
				.filter(nonActivityClassesWhenWorkflowIsNotEnabled());

		final JSONArray jsonDomains = new JSONArray();
		for (final CMDomain domain : domains) {
			jsonDomains.put(domainSerializer().toClient(domain, activeOnly));
		}
		final JSONObject out = new JSONObject();
		out.put(DOMAINS, jsonDomains);
		return out;
	}

	private <T extends CMDomain> Predicate<T> domainsWithActiveClasses() {
		final Predicate<T> predicate = new Predicate<T>() {
			@Override
			public boolean apply(final T input) {
				return input.getClass1().isActive() && input.getClass2().isActive();
			}
		};
		return predicate;
	}

	private <T extends CMDomain> Predicate<T> nonActivityClassesWhenWorkflowIsNotEnabled() {
		final boolean workflowEnabled = workflowLogic().isWorkflowEnabled();
		final CMClass activityClass = userDataView().getActivityClass();
		return new Predicate<T>() {
			@Override
			public boolean apply(final T input) {
				final boolean class1IsActivity = activityClass.isAncestorOf(input.getClass1());
				final boolean class2IsActivity = activityClass.isAncestorOf(input.getClass2());
				return (!class1IsActivity && !class2IsActivity) ? true : workflowEnabled;
			}
		};
	}

	@Admin
	@JSONExported
	public JSONObject saveDomain( //
			@Parameter(value = ID) final long domainId, //
			@Parameter(value = NAME, required = false) final String domainName, //
			@Parameter(value = DOMAIN_FIRST_CLASS_ID, required = false) final int classId1, //
			@Parameter(value = DOMAIN_SECOND_CLASS_ID, required = false) final int classId2, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = DOMAIN_CARDINALITY, required = false) final String cardinality, //
			@Parameter(value = DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS) final String descriptionDirect, //
			@Parameter(value = DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS) final String descriptionInverse, //
			@Parameter(value = DOMAIN_IS_MASTER_DETAIL) final boolean isMasterDetail, //
			@Parameter(value = DOMAIN_MASTER_DETAIL_LABEL, required = false) final String mdLabel, //
			@Parameter(value = ACTIVE) final boolean isActive, //
			@Parameter(value = DISABLED1, required = false) final JSONArray disabled1, //
			@Parameter(value = DISABLED2, required = false) final JSONArray disabled2 //
	) throws JSONException, AuthException, NotFoundException {
		final Domain domain = Domain.newDomain() //
				.withName(domainName) //
				.withIdClass1(classId1) //
				.withIdClass2(classId2) //
				.withDescription(description) //
				.withCardinality(cardinality) //
				.withDirectDescription(descriptionDirect) //
				.withInverseDescription(descriptionInverse) //
				.thatIsMasterDetail(isMasterDetail) //
				.withMasterDetailDescription(mdLabel) //
				.thatIsActive(isActive) //
				.withDisabled1((disabled1 == null) ? NO_DISABLED : toIterable(disabled1)) //
				.withDisabled2((disabled2 == null) ? NO_DISABLED : toIterable(disabled2)) //
				.build();
		final CMDomain createdOrUpdated;
		if (domainId == -1) {
			createdOrUpdated = dataDefinitionLogic().create(domain);
		} else {
			createdOrUpdated = dataDefinitionLogic().update(domain);
		}
		return domainSerializer().toClient(createdOrUpdated, false, DOMAIN);
	}

	@JSONExported
	public void deleteDomain(@Parameter(value = DOMAIN_NAME, required = false) final String domainName //
	) throws JSONException {

		dataDefinitionLogic().deleteDomainIfExists(domainName);
	}

	@JSONExported
	public JSONObject getDomainList( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = SKIP_DISABLED_CLASSES, required = false) final boolean skipDisabledClasses //
	) throws JSONException {
		final JSONArray jsonDomains = new JSONArray();
		// TODO system really needed
		final Iterable<CMDomain> domains = from(
				systemDataAccessLogic().findDomainsForClass(className, skipDisabledClasses)) //
				.filter(not(isSystem(CMDomain.class)));
		for (final CMDomain domain : domains) {
			jsonDomains.put(domainSerializer().toClient(domain, className));
		}
		final JSONObject out = new JSONObject();
		out.put(DOMAINS, jsonDomains);
		return out;
	}

	/**
	 * Given a class name, this method retrieves all the attributes for all the
	 * SIMPLE classes that have at least one attribute of type foreign key whose
	 * target class is the specified class or an ancestor of it
	 * 
	 * @param className
	 * @return
	 * @throws Exception
	 */
	@JSONExported
	public JSONArray getFKTargetingClass( //
			@Parameter(CLASS_NAME) final String className //
	) throws Exception {

		// TODO: improve performances by getting only simple classes (the
		// database should filter the simple classes)
		final DataAccessLogic logic = userDataAccessLogic();
		final CMClass targetClass = logic.findClass(className);
		final JSONArray fk = new JSONArray();

		for (final CMClass activeClass : logic.findActiveClasses()) {
			final boolean isSimpleClass = !activeClass.holdsHistory();

			if (isSimpleClass) {
				for (final CMAttribute attribute : activeClass.getActiveAttributes()) {
					final String referencedClassName = attribute.getForeignKeyDestinationClassName();
					if (referencedClassName == null) {
						continue;
					}

					final CMClass referencedClass = logic.findClass(referencedClassName);
					if (referencedClass.isAncestorOf(targetClass)) {
						final boolean serializeAlsoClassId = true;
						final AttributeSerializer attributeSerializer = AttributeSerializer.newInstance() //
								.withDataView(systemDataView()) //
								.build();
						final JSONObject jsonAttribute = attributeSerializer.toClient(attribute, serializeAlsoClassId);

						fk.put(jsonAttribute);
					}
				}
			}
		}

		return fk;
	}

	/**
	 * Retrieves all domains with cardinality 1:N or N:1 in which the class with
	 * the specified name is on the 'N' side
	 * 
	 * @param className
	 * @return
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public JSONObject getReferenceableDomainList(@Parameter(CLASS_NAME) final String className) throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		final Iterable<? extends CMDomain> referenceableDomains = systemDataAccessLogic().findReferenceableDomains(
				className);
		for (final CMDomain domain : referenceableDomains) {
			jsonDomains.put(domainSerializer().toClient(domain, false));
		}
		out.put(DOMAINS, jsonDomains);
		return out;
	}

	@JSONExported
	public JsonResponse getFunctions() {
		return JsonResponse.success( //
				from(dataDefinitionLogic().functions()) //
						.transform(toJsonFunction) //
						.toList());
	}

}
