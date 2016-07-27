package org.cmdbuild.spring.configuration;

import org.cmdbuild.logic.DefaultNavigationTreeLogic;
import org.cmdbuild.logic.NavigationTreeLogic;
import org.cmdbuild.services.store.DBDomainTreeStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NavigationTree {

	@Autowired
	private Data data;

	@Bean
	public NavigationTreeLogic navigationTreeLogic() {
		return new DefaultNavigationTreeLogic(dbDomainTreeStore());
	}

	@Bean
	protected DBDomainTreeStore dbDomainTreeStore() {
		return new DBDomainTreeStore(data.systemDataView());
	}

}
