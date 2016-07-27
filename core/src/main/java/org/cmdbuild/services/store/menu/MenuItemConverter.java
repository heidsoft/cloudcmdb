package org.cmdbuild.services.store.menu;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.model.view.ViewConverter.VIEW_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.GROUP_NAME_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.NUMBER_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.TYPE_ATTRIBUTE;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.custompage.DBCustomPageConverter;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.custompages.CustomPage;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.view.View;

public class MenuItemConverter {

	private static final String NO_GROUP_NAME = "";
	private static final int NO_INDEX = 0;
	private static final Integer NO_REFERENCED_ELEMENT_ID = 0;

	protected final CMDataView dataView;
	private final DataAccessLogic dataAccessLogic;

	public MenuItemConverter( //
			final CMDataView dataView, //
			final SystemDataAccessLogicBuilder dataAccessLogicBuilder //
	) {
		this(dataView, dataAccessLogicBuilder.build());
	}

	protected MenuItemConverter( //
			final CMDataView dataView, //
			final DataAccessLogic dataAccessLogic) {
		this.dataView = dataView;
		this.dataAccessLogic = dataAccessLogic;
	}

	/**
	 * 
	 * @param groupName
	 * @param menuItem
	 * @return a menuCard for the given menuItem to assign to the given group
	 *         name
	 */
	public CMCardDefinition toMenuCard(final String groupName, final MenuItem menuItem) {
		final MenuItemType type = menuItem.getType();
		final MenuItemConverter converterStrategy;

		if (MenuItemType.REPORT_CSV.equals(type) || MenuItemType.REPORT_PDF.equals(type)) {
			converterStrategy = new ReportConverterStrategy(this);
		} else if (MenuItemType.FOLDER.equals(type)) {
			converterStrategy = new FolderConverterStrategy(this);
		} else if (MenuItemType.DASHBOARD.equals(type)) {
			converterStrategy = new DashboardConverterStrategy(this);
		} else if (MenuItemType.VIEW.equals(type)) {
			converterStrategy = new ViewConverterStrategy(this);
		} else if (MenuItemType.CUSTOM_PAGE.equals(type)) {
			converterStrategy = new CustomPageConverterStrategy(this);
		} else {
			converterStrategy = new EntryTypeConverterStrategy(this);
		}

		return converterStrategy.fromMenuItemToMenuCard(groupName, menuItem);
	}

	/**
	 * 
	 * @param elements
	 * @return a MenuItem starting to a list of MenuElement
	 */
	public MenuItem fromMenuElement(final Iterable<MenuElement> elements) {
		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);
		final Map<Number, ConvertingItem> items = new HashMap<Number, ConvertingItem>();
		for (final MenuElement menuElement : elements) {
			final Number id = menuElement.getId();
			try {
				items.put(id, convertMenuElementToMenuItemBuilder(menuElement));
			} catch (final Exception e) {
				Log.CMDBUILD.debug("Error converting MenuItem");
			}
		}
		for (final ConvertingItem item : items.values()) {
			final Number parentId = item.menuElement.getParentId();
			if (parentId.longValue() > 0) {
				final ConvertingItem parent = items.get(parentId.longValue());
				parent.menuItem.addChild(item.menuItem);
			} else {
				root.addChild(item.menuItem);
			}
		}
		return root;
	}

	/**
	 * 
	 * @param cmClass
	 * @return a MenuItem that is the menu representation of a CMClass
	 */
	public MenuItem fromCMClass(final CMClass cmClass, final CMDataView dataView) {
		final MenuItem menuItem = new MenuItemDTO();

		final CMClass activity = dataView.findClass(Constants.BASE_PROCESS_CLASS_NAME);
		if (activity != null && activity.isAncestorOf(cmClass)) {

			menuItem.setType(MenuItemType.PROCESS);
		} else {
			menuItem.setType(MenuItemType.CLASS);
		}

		menuItem.setReferedClassName(cmClass.getIdentifier().getLocalName());
		menuItem.setReferencedElementId(NO_REFERENCED_ELEMENT_ID);
		menuItem.setDescription(cmClass.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	// FIXME when implement new ReportCard use it as parameter
	public MenuItem fromCMReport(final CMCard report, final ReportExtension extension) {
		final MenuItem menuItem = new MenuItemDTO();
		if (ReportExtension.CSV.equals(extension)) {
			menuItem.setType(MenuItemType.REPORT_CSV);
		} else if (ReportExtension.PDF.equals(extension)) {
			menuItem.setType(MenuItemType.REPORT_PDF);
		}

		menuItem.setReferedClassName(report.getType().getIdentifier().getLocalName());
		menuItem.setReferencedElementId(Integer.valueOf(report.getId().toString()));
		menuItem.setDescription((String) report.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	public MenuItem fromDashboard(final DashboardDefinition dashboardDefinition, final Integer id) {
		final MenuItem menuItem = new MenuItemDTO();

		menuItem.setType(MenuItemType.DASHBOARD);
		menuItem.setReferedClassName("_Dashboard");
		menuItem.setReferencedElementId(id);
		menuItem.setDescription(dashboardDefinition.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	public MenuItem fromView(final View view) {
		final MenuItem menuItem = new MenuItemDTO();

		menuItem.setType(MenuItemType.VIEW);
		menuItem.setReferedClassName(VIEW_CLASS_NAME);
		menuItem.setReferencedElementId(view.getId());
		menuItem.setDescription(view.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);

		return menuItem;
	}

	public MenuItem fromCustomPage(final CustomPage customPage) {
		final MenuItem menuItem = new MenuItemDTO();

		menuItem.setType(MenuItemType.CUSTOM_PAGE);
		menuItem.setReferedClassName(DBCustomPageConverter.CLASSNAME);
		menuItem.setReferencedElementId(customPage.getId());
		menuItem.setDescription(customPage.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);

		return menuItem;
	}

	private ConvertingItem convertMenuElementToMenuItemBuilder(final MenuElement menuElement) {
		final MenuItem menuItem = new MenuItemDTO();
		menuItem.setId(new Long(menuElement.getId()));
		menuItem.setUniqueIdentifier(menuElement.getCode());
		menuItem.setType(menuElement.getType());
		menuItem.setDescription(menuElement.getDescription());
		menuItem.setParentId(menuElement.getParentId());
		menuItem.setIndex(menuElement.getNumber());

		if (!MenuItemType.FOLDER.equals(menuItem.getType())) {
			menuItem.setReferedClassName(menuElement.getElementClassName());
			menuItem.setReferencedElementId(menuElement.getElementId());

			if (MenuItemType.VIEW.equals(menuItem.getType())) {
				final Card viewCard = dataAccessLogic.fetchCard(menuItem.getReferedClassName(), //
						menuItem.getReferencedElementId().longValue() //
						);

				final Map<String, Object> specificTypeValues = new HashMap<String, Object>();
				specificTypeValues.put("type", viewCard.getAttribute("Type"));
				specificTypeValues.put("filter", viewCard.getAttribute("Filter"));
				specificTypeValues.put("sourceFunction", viewCard.getAttribute("SourceFunction"));
				final Long classId = (Long) viewCard.getAttribute("IdSourceClass");
				final CMClass cmClass = dataView.findClass(classId);
				if (cmClass != null) {
					specificTypeValues.put("sourceClassName", cmClass.getIdentifier().getLocalName());
				}

				menuItem.setSpecificTypeValues(specificTypeValues);
			}
		}
		menuItem.setGroupName(menuElement.getGroupName());
		return new ConvertingItem(menuElement, menuItem);
	}

	CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
		final CMCardDefinition menuCard = dataView.createCardFor(dataView.findClass(MENU_CLASS_NAME));

		final String UUID = defaultIfNull(menuItem.getUniqueIdentifier(), java.util.UUID.randomUUID().toString());

		final String typeAsString = menuItem.getType().getValue();
		menuCard.setCode(UUID);
		menuCard.set(TYPE_ATTRIBUTE, typeAsString);
		menuCard.setDescription(menuItem.getDescription());
		menuCard.set(NUMBER_ATTRIBUTE, menuItem.getIndex());
		menuCard.set(GROUP_NAME_ATTRIBUTE, groupName);
		return menuCard;
	}

	private static class FolderConverterStrategy extends MenuItemConverter {

		public FolderConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			return super.fromMenuItemToMenuCard(groupName, menuItem);
		}

	}

	private static class EntryTypeConverterStrategy extends MenuItemConverter {

		public EntryTypeConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE,
					(menuItem.getReferencedElementId() == null) ? 0 : menuItem.getReferencedElementId());
			final Long referedClassId = dataView.findClass(menuItem.getReferedClassName()).getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, referedClassId);
			return menuCard;
		}

	}

	private static class ReportConverterStrategy extends MenuItemConverter {

		public ReportConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE, menuItem.getReferencedElementId());
			final Long reportClassId = dataView.findClass("Report").getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, reportClassId);
			return menuCard;
		}

	}

	private static class DashboardConverterStrategy extends MenuItemConverter {

		public DashboardConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE, menuItem.getReferencedElementId());
			final Long dashboardClassId = dataView.findClass("_Dashboards").getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, dashboardClassId);
			return menuCard;
		}

	}

	private static class ViewConverterStrategy extends MenuItemConverter {

		public ViewConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE, menuItem.getReferencedElementId());
			final Long viewClassId = dataView.findClass(VIEW_CLASS_NAME).getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, viewClassId);
			return menuCard;
		}

	}

	private static class CustomPageConverterStrategy extends MenuItemConverter {

		public CustomPageConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE, menuItem.getReferencedElementId());
			final Long classId = dataView.findClass(DBCustomPageConverter.CLASSNAME).getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, classId);
			return menuCard;
		}

	}

	private static class ConvertingItem {
		public final MenuElement menuElement;
		public final MenuItem menuItem;

		public ConvertingItem(final MenuElement menuElement, final MenuItem menuItem) {
			this.menuElement = menuElement;
			this.menuItem = menuItem;
		}
	}

}
