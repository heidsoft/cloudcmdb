package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.GroupFetcher;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.data.store.MenuElementStore;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.menu.DefaultMenuLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.services.localization.LocalizedStorableConverter;
import org.cmdbuild.services.store.menu.DataViewMenuStore;
import org.cmdbuild.services.store.menu.MenuElement;
import org.cmdbuild.services.store.menu.MenuElementConverter;
import org.cmdbuild.services.store.menu.MenuItemConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Menu {

	@Autowired
	private CustomPages customPages;

	@Autowired
	private DashboardLogic dashboardLogic;

	@Autowired
	private Data data;

	@Autowired
	private GroupFetcher groupFetcher;

	@Autowired
	private Report report;

	@Autowired
	private SystemDataAccessLogicBuilder systemDataAccessLogicBuilder;

	@Autowired
	private Translation translation;

	@Autowired
	private User user;

	@Autowired
	private UserStore userStore;

	@Autowired
	private View view;

	@Bean
	@Scope(PROTOTYPE)
	public MenuLogic menuLogic() {
		return new DefaultMenuLogic(dataViewMenuStore());
	}

	@Bean
	public MenuItemConverter menuItemConverter() {
		return new MenuItemConverter(data.systemDataView(), systemDataAccessLogicBuilder);
	}

	@Bean
	protected DataViewStore<MenuElement> baseMenuElementStore() {
		return DataViewStore.<MenuElement> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(menuElementStorableConverter()) //
				.build();
	}

	private MenuElementConverter menuElementConverter() {
		return new MenuElementConverter(data.systemDataView(), user.userDataAccessLogicBuilder());
	}

	private StorableConverter<MenuElement> menuElementStorableConverter() {
		return new LocalizedStorableConverter<MenuElement>(menuElementConverter(), translation.translationFacade(),
				data.systemDataView(), report.reportLogic());
	}

	@Bean
	@Scope(PROTOTYPE)
	public DataViewMenuStore dataViewMenuStore() {
		return new DataViewMenuStore( //
				data.systemDataView(), //
				groupFetcher, //
				dashboardLogic, //
				user.userDataAccessLogicBuilder(), //
				view.viewLogic(), //
				customPages.defaultCustomPagesLogic(), //
				menuItemConverter(), //
				userStore.getUser(), //
				menuElementStore());
	}

	private MenuElementStore menuElementStore() {
		return new MenuElementStore(baseMenuElementStore(), data.systemDataView(), userStore, view.viewConverter(),
				menuElementStorableConverter(), customPages.defaultCustomPagesLogic());
	}

}
