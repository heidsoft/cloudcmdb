package org.cmdbuild.services.store.menu;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.services.store.menu.MenuConstants.DEFAULT_MENU_GROUP_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.GROUP_NAME_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.PARENT_ID_ATTRIBUTE;

import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.GroupFetcher;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.MenuElementStore;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.custompages.CustomPage;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataViewCardFetcher;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.view.View;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * MenuElementStore is now used for reading elements in order to support
 * localizations. Refactoring should be completed using MenuElementStore also
 * for save and delete operations.
 **/
public class DataViewMenuStore implements MenuStore {

	private final CMDataView view;
	private final GroupFetcher groupFetcher;
	private final DashboardLogic dashboardLogic;
	private final DataAccessLogic dataAccessLogic;
	private final ViewLogic viewLogic;
	private final CustomPagesLogic customPagesLogic;
	private final MenuItemConverter converter;
	private final OperationUser operationUser;
	private final MenuElementStore menuElementStore;

	public DataViewMenuStore( //
			final CMDataView view, //
			final GroupFetcher groupFetcher, //
			final DashboardLogic dashboardLogic, //
			final UserDataAccessLogicBuilder dataAccessLogicBuilder, //
			final ViewLogic viewLogic, //
			final CustomPagesLogic customPagesLogic, //
			final MenuItemConverter converter, //
			final OperationUser operationUser, //
			final MenuElementStore menuElementStore) {
		this.view = view;
		this.groupFetcher = groupFetcher;
		this.dashboardLogic = dashboardLogic;
		this.dataAccessLogic = dataAccessLogicBuilder.build();
		this.viewLogic = viewLogic;
		this.customPagesLogic = customPagesLogic;
		this.converter = converter;
		this.operationUser = operationUser;
		this.menuElementStore = menuElementStore;
	}

	@Override
	public MenuItem read(final String groupName) {
		final String _groupName = defaultIfBlank(groupName, DEFAULT_MENU_GROUP_NAME);

		final Iterable<MenuElement> elements = menuElementStore.readAll(new Groupable() {
			@Override
			public String getGroupAttributeName() {
				return GROUP_NAME_ATTRIBUTE;
			}

			@Override
			public Object getGroupAttributeValue() {
				return _groupName;
			}
		});
		return converter.fromMenuElement(elements);
	}

	@Override
	public void delete(String groupName) {
		groupName = defaultIfBlank(groupName, DEFAULT_MENU_GROUP_NAME);

		final Iterable<CMCard> cardsToDelete = fetchMenuCardsForGroup(groupName);
		for (final CMCard cardToDelete : cardsToDelete) {
			view.delete(cardToDelete);
		}
	}

	@Override
	public void save(String groupName, final MenuItem menuItem) {
		groupName = defaultIfBlank(groupName, DEFAULT_MENU_GROUP_NAME);
		delete(groupName);
		saveNode(groupName, menuItem, null);
	}

	@Override
	public MenuItem getAvailableItems(final String groupName) {
		final Iterable<CMCard> menuCards = fetchMenuCardsForGroup(defaultIfBlank(groupName, DEFAULT_MENU_GROUP_NAME));
		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);
		root.addChild(getAvailableClasses(menuCards));
		root.addChild(getAvailableProcesses(menuCards));
		root.addChild(getAvailableReports(menuCards));
		root.addChild(getAvailableDashboards(menuCards));
		root.addChild(getAvailableViews(menuCards));
		root.addChild(getAvailableCustomPages(menuCards));
		return root;
	}

	private Iterable<CMCard> fetchMenuCardsForGroup(final String groupName) {
		final List<CMCard> menuCards = Lists.newArrayList();
		final CMClass menuClass = view.findClass(MENU_CLASS_NAME);
		final CMQueryResult result = view.select(anyAttribute(menuClass)) //
				.from(menuClass) //
				.where(condition(attribute(menuClass, GROUP_NAME_ATTRIBUTE), eq(groupName))) //
				.run();
		for (final CMQueryRow row : result) {
			menuCards.add(row.getCard(menuClass));
		}
		return menuCards;
	}

	@Override
	public MenuItem getMenuToUseForGroup(final String groupName) {
		final CMGroup group = groupFetcher.fetchGroupWithName(groupName);
		Iterable<MenuElement> menuElementsForGroup = menuElementStore.readAndFilter(groupName, group);
		if (Iterables.isEmpty(menuElementsForGroup)) {
			menuElementsForGroup = menuElementStore.readAndFilter(DEFAULT_MENU_GROUP_NAME, group);
		}
		return converter.fromMenuElement(menuElementsForGroup);
	}

	private void saveNode(final String groupName, final MenuItem menuItem, final Long parentId) {
		Long savedNodeId = null;
		// The root node is not useful, and is not saved on DB
		if (!menuItem.getType().equals(MenuItemType.ROOT)) {
			final CMCardDefinition mutableMenuCard = converter.toMenuCard(groupName, menuItem);
			if (parentId == null) {
				mutableMenuCard.set(PARENT_ID_ATTRIBUTE, 0);
			} else {
				mutableMenuCard.set(PARENT_ID_ATTRIBUTE, parentId);
			}
			mutableMenuCard.setUser(operationUser.getAuthenticatedUser().getUsername());
			final CMCard savedCard = mutableMenuCard.save();
			savedNodeId = savedCard.getId();
		}
		for (final MenuItem child : menuItem.getChildren()) {
			saveNode(groupName, child, savedNodeId);
		}
	}

	private MenuItem getAvailableClasses(final Iterable<CMCard> menuCards) {
		final MenuItem classesFolder = new MenuItemDTO();
		classesFolder.setType(MenuItemType.FOLDER);
		classesFolder.setDescription("class");
		classesFolder.setIndex(0);
		for (final CMClass cmClass : view.findClasses()) {
			if (cmClass.isSystem() || cmClass.isBaseClass() || isInTheMenuList(cmClass, menuCards)
					|| dataAccessLogic.isProcess(cmClass)) {
				continue;
			}
			classesFolder.addChild(converter.fromCMClass(cmClass, view));
		}
		return classesFolder;
	}

	private MenuItem getAvailableProcesses(final Iterable<CMCard> menuCards) {

		final MenuItem processesFolder = new MenuItemDTO();
		processesFolder.setType(MenuItemType.FOLDER);
		processesFolder.setDescription("processclass");
		processesFolder.setIndex(1);

		for (final CMClass cmClass : view.findClasses()) {
			if (cmClass.isSystem() || isInTheMenuList(cmClass, menuCards) || !dataAccessLogic.isProcess(cmClass)) {
				continue;
			}

			processesFolder.addChild(converter.fromCMClass(cmClass, view));
		}

		return processesFolder;
	}

	private MenuItem getAvailableReports(final Iterable<CMCard> menuCards) {
		final CMClass reportTable = view.getReportClass();

		final MenuItem reportFolder = new MenuItemDTO();
		reportFolder.setType(MenuItemType.FOLDER);
		reportFolder.setDescription("report");
		reportFolder.setIndex(2);

		final PagedElements<CMCard> reports = DataViewCardFetcher.newInstance() //
				.withDataView(view) //
				.withClassName(reportTable.getIdentifier().getLocalName()) //
				.withQueryOptions(QueryOptions.newQueryOption().build()) //
				.build() //
				.fetch();

		for (final CMCard report : reports) {
			for (final ReportExtension extension : ReportExtension.values()) {
				if (thereIsNotAlreadyInTheMenu(report, extension, menuCards)) {
					reportFolder.addChild(converter.fromCMReport(report, extension));
				}
			}
		}

		return reportFolder;
	}

	private MenuItem getAvailableDashboards(final Iterable<CMCard> menuCards) {
		final MenuItem dashboardFolder = new MenuItemDTO();
		dashboardFolder.setType(MenuItemType.FOLDER);
		dashboardFolder.setDescription("dashboard");
		dashboardFolder.setIndex(3);
		final Map<Integer, DashboardDefinition> dashboards = dashboardLogic.fullListDashboards();
		for (final Integer id : dashboards.keySet()) {
			if (!isInTheMenuList(id, menuCards)) {
				dashboardFolder.addChild(converter.fromDashboard(dashboards.get(id), id));
			}
		}

		return dashboardFolder;
	}

	private MenuItem getAvailableViews(final Iterable<CMCard> menuCards) {
		final MenuItem viewsFolder = new MenuItemDTO();
		viewsFolder.setType(MenuItemType.FOLDER);
		viewsFolder.setDescription("view");
		viewsFolder.setIndex(4);

		final List<View> definedViews = viewLogic.fetchViewsOfAllTypes();

		for (final View view : definedViews) {
			final Integer id = new Integer(view.getId().intValue());
			if (!isInTheMenuList(id, menuCards)) {
				viewsFolder.addChild(converter.fromView(view));
			}
		}

		return viewsFolder;
	}

	private MenuItem getAvailableCustomPages(final Iterable<CMCard> menuCards) {
		final MenuItem viewsFolder = new MenuItemDTO();
		viewsFolder.setType(MenuItemType.FOLDER);
		viewsFolder.setDescription("custompage");
		viewsFolder.setIndex(5);

		for (final CustomPage element : customPagesLogic.read()) {
			final Integer id = element.getId().intValue();
			if (!isInTheMenuList(id, menuCards)) {
				viewsFolder.addChild(converter.fromCustomPage(element));
			}
		}

		return viewsFolder;
	}

	private boolean thereIsNotAlreadyInTheMenu(final CMCard report, final ReportExtension extension,
			final Iterable<CMCard> menuCards) {
		for (final CMCard menuCard : menuCards) {
			final String suffix = extension.getExtension();
			if (menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE) == report.getId()
					&& ((String) menuCard.getCode()).endsWith((suffix))) {
				return false;
			}
		}

		return true;
	}

	private boolean isInTheMenuList(final Integer id, final Iterable<CMCard> menuCards) {
		for (final CMCard menuCard : menuCards) {
			final Object elementObjectId = menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE);
			if (elementObjectId != null) {
				if (((Integer) elementObjectId).equals(id)) {

					return true;
				}
			}
		}
		return false;
	}

	private boolean isInTheMenuList(final CMClass cmClass, final Iterable<CMCard> menuCards) {
		if (menuCards == null) {
			return false;
		}
		for (final CMCard menuCard : menuCards) {
			final Long elementClassId = menuCard.get(ELEMENT_CLASS_ATTRIBUTE, Long.class);
			if (elementClassId != null && !menuCard.get("Type").equals(MenuItemType.FOLDER.getValue())) {
				if (elementClassId.equals(cmClass.getId())) {
					return true;
				}
			}
		}
		return false;
	}
}
