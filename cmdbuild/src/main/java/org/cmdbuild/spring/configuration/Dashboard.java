package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.services.store.DBDashboardStore;
import org.cmdbuild.services.store.DashboardStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Dashboard {

	@Autowired
	private Data data;

	@Autowired
	private UserStore userStore;

	@Bean
	public DashboardStore dashboardStore() {
		return new DBDashboardStore(data.systemDataView());
	}

	@Bean
	@Scope(PROTOTYPE)
	public DashboardLogic dashboardLogic() {
		return new DashboardLogic(data.systemDataView(), dashboardStore(), userStore.getUser());
	}

}
