package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;
import static org.cmdbuild.spring.util.Constants.SYSTEM;
import static org.cmdbuild.spring.util.Constants.USER;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.auth.context.DefaultPrivilegeContextFactory;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.dao.view.user.privileges.PartiallyCachingRowAndColumnPrivilegeFetcher;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.DataViewRowAndColumnPrivilegeFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PrivilegeManagement {

	@Autowired
	private Data data;

	@Autowired
	private UserStore userStore;

	/**
	 * Not used for DI but just to have two {@link PrivilegeContext} beans
	 * managed. In this way the {@link Qualifier} annotation must be used.
	 */
	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(USER)
	public PrivilegeContext userPrivilegeContext() {
		return userStore.getUser().getPrivilegeContext();
	}

	@Bean
	@Qualifier(SYSTEM)
	public SystemPrivilegeContext systemPrivilegeContext() {
		return new SystemPrivilegeContext();
	}

	@Bean
	public PrivilegeContextFactory privilegeContextFactory() {
		return new DefaultPrivilegeContextFactory();
	}

	@Bean
	@Scope(PROTOTYPE)
	public RowAndColumnPrivilegeFetcher rowAndColumnPrivilegeFetcher() {
		return new PartiallyCachingRowAndColumnPrivilegeFetcher(new DataViewRowAndColumnPrivilegeFetcher(
				data.systemDataView(), userPrivilegeContext(), userStore));
	}

}
