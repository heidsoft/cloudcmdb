package org.cmdbuild.services.store.menu;

import static org.cmdbuild.logic.report.Predicates.currentGroupAllowed;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.TYPE_ATTRIBUTE;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.privileges.predicates.IsAlwaysReadable;
import org.cmdbuild.privileges.predicates.IsReadableClass;
import org.cmdbuild.privileges.predicates.IsReadableCustomPage;
import org.cmdbuild.privileges.predicates.IsReadableDashboard;
import org.cmdbuild.privileges.predicates.IsReadableReport;
import org.cmdbuild.privileges.predicates.IsReadableView;
import org.cmdbuild.services.store.report.ReportStore;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class MenuCardPredicateFactory {

	private final CMGroup group;
	private final CMDataView dataView;
	private final Supplier<PrivilegeContext> privilegeContext;
	private final StorableConverter<View> viewConverter;
	private final UserStore userStore;
	private final CustomPagesLogic customPagesLogic;

	public MenuCardPredicateFactory( //
			final CMDataView view, //
			final CMGroup group, //
			final Supplier<PrivilegeContext> privilegeContext, //
			final StorableConverter<View> viewConverter, //
			final UserStore userStore, //
			final CustomPagesLogic customPagesLogic //
	) {
		this.group = group;
		this.dataView = view;
		this.privilegeContext = privilegeContext;
		this.viewConverter = viewConverter;
		this.userStore = userStore;
		this.customPagesLogic = customPagesLogic;
	}

	// TODO: change it (privileges on processes and reports)
	public Predicate<CMCard> getPredicate(final CMCard menuCard) {
		Validate.isTrue(menuCard.getType().getName().equals(MENU_CLASS_NAME));

		if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.CLASS.getValue())) {
			return new IsReadableClass(dataView, privilegeContext.get());
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.FOLDER.getValue())) {
			return new IsAlwaysReadable();
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.ROOT.getValue())) {
			return new IsAlwaysReadable();
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.PROCESS.getValue())) {
			return new IsReadableClass(dataView, privilegeContext.get());
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.REPORT_CSV.getValue())) {
			return new IsReadableReport(applicationContext().getBean(ReportStore.class), currentGroupAllowed(userStore));
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.REPORT_PDF.getValue())) {
			return new IsReadableReport(applicationContext().getBean(ReportStore.class), currentGroupAllowed(userStore));
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.DASHBOARD.getValue())) {
			return new IsReadableDashboard(dataView, group);
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.VIEW.getValue())) {
			return new IsReadableView(dataView, privilegeContext.get(), viewConverter);
		} else if (menuCard.get(TYPE_ATTRIBUTE).equals(MenuItemType.CUSTOM_PAGE.getValue())) {
			return new IsReadableCustomPage(privilegeContext.get(), customPagesLogic);
		}
		throw new IllegalArgumentException();
	}

}
