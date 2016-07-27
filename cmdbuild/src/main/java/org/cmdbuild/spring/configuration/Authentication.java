package org.cmdbuild.spring.configuration;

import static java.lang.Long.MAX_VALUE;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.cmdbuild.spring.util.Constants.DEFAULT;
import static org.cmdbuild.spring.util.Constants.SOAP;

import java.util.Map.Entry;
import java.util.function.Predicate;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.CasAuthenticator;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.HeaderAuthenticator;
import org.cmdbuild.auth.LdapAuthenticator;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.NotSystemUserFetcher;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.config.CmdbuildConfiguration.ChangeListener;
import org.cmdbuild.data.store.CachingStore;
import org.cmdbuild.data.store.session.DefaultSessionStore;
import org.cmdbuild.data.store.session.Session;
import org.cmdbuild.data.store.session.SessionStore;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.DefaultAuthenticationLogic;
import org.cmdbuild.logic.auth.DefaultGroupsLogic;
import org.cmdbuild.logic.auth.DefaultSessionLogic;
import org.cmdbuild.logic.auth.DefaultSessionLogic.CurrentSessionStore;
import org.cmdbuild.logic.auth.DefaultSessionLogic.ThreadLocalCurrentSessionStore;
import org.cmdbuild.logic.auth.GroupsLogic;
import org.cmdbuild.logic.auth.RestSessionLogic;
import org.cmdbuild.logic.auth.SoapSessionLogic;
import org.cmdbuild.logic.auth.StandardSessionLogic;
import org.cmdbuild.logic.auth.TransactionalGroupsLogic;
import org.cmdbuild.privileges.DBGroupFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.CustomPagePrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.FilterPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.cmdbuild.services.soap.security.SoapConfiguration;
import org.cmdbuild.services.soap.security.SoapPasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Configuration
public class Authentication {

	@Autowired
	private AuthenticationStore authenticationStore;

	@Autowired
	private CustomPages customPages;

	@Autowired
	private Data data;

	@Autowired
	private Filter filter;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Autowired
	private Properties properties;

	@Autowired
	private SoapConfiguration soapConfiguration;

	@Autowired
	private UserStore userStore;

	@Autowired
	private View view;

	@Autowired
	private Web web;

	@Bean
	@Qualifier(DEFAULT)
	protected LegacyDBAuthenticator dbAuthenticator() {
		return new LegacyDBAuthenticator(data.systemDataView());
	}

	@Bean
	@Qualifier(SOAP)
	protected NotSystemUserFetcher notSystemUserFetcher() {
		return new NotSystemUserFetcher(data.systemDataView(), authenticationStore);
	}

	@Bean
	protected SoapPasswordAuthenticator soapPasswordAuthenticator() {
		return new SoapPasswordAuthenticator();
	}

	@Bean
	protected CasAuthenticator casAuthenticator() {
		return new CasAuthenticator(properties.authConf());
	}

	@Bean
	protected HeaderAuthenticator headerAuthenticator() {
		return new HeaderAuthenticator(properties.authConf());
	}

	@Bean
	protected LdapAuthenticator ldapAuthenticator() {
		return new LdapAuthenticator(properties.authConf());
	}

	@Bean
	public DBGroupFetcher dbGroupFetcher() {
		return new DBGroupFetcher(data.systemDataView(),
				asList(new CMClassPrivilegeFetcherFactory(data.systemDataView()), new ViewPrivilegeFetcherFactory(
						data.systemDataView(), view.viewConverter()),
				new FilterPrivilegeFetcherFactory(data.systemDataView(), filter.dataViewFilterStore()),
				new CustomPagePrivilegeFetcherFactory(data.systemDataView(), customPages.defaultCustomPagesLogic())));
	}

	@Bean
	public AuthenticationService defaultAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(
				properties.authConf(), data.systemDataView());
		authenticationService.setPasswordAuthenticators(dbAuthenticator(), ldapAuthenticator());
		authenticationService.setClientRequestAuthenticators(headerAuthenticator(), casAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		return authenticationService;
	}

	@Bean
	protected AuthenticationService soapAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(soapConfiguration,
				data.systemDataView());
		authenticationService.setPasswordAuthenticators(soapPasswordAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator(), notSystemUserFetcher());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		return authenticationService;
	}

	@Bean
	protected AuthenticationService restAuthenticationService() {
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(
				properties.authConf(), data.systemDataView());
		authenticationService.setPasswordAuthenticators(dbAuthenticator(), ldapAuthenticator());
		authenticationService.setClientRequestAuthenticators(headerAuthenticator(), casAuthenticator());
		authenticationService.setUserFetchers(dbAuthenticator(), notSystemUserFetcher());
		authenticationService.setGroupFetcher(dbGroupFetcher());
		return authenticationService;
	}

	@Bean
	public StandardSessionLogic standardSessionLogic() {
		final DefaultAuthenticationLogic delegate = new DefaultAuthenticationLogic(defaultAuthenticationService(),
				privilegeManagement.privilegeContextFactory(), data.systemDataView(), userStore);
		return new StandardSessionLogic(new DefaultSessionLogic(delegate, sessionStore(), userStore,
				defaultSessionStore(), web.simpleTokenGenerator(), canImpersonate()));
	}

	@Bean
	public SoapSessionLogic soapSessionLogic() {
		final AuthenticationLogic delegate = new DefaultAuthenticationLogic(soapAuthenticationService(),
				privilegeManagement.privilegeContextFactory(), data.systemDataView(), userStore);
		return new SoapSessionLogic(new DefaultSessionLogic(delegate, sessionStore(), userStore, defaultSessionStore(),
				web.simpleTokenGenerator(), canImpersonate()));
	}

	@Bean
	public RestSessionLogic restSessionLogic() {
		final AuthenticationLogic delegate = new DefaultAuthenticationLogic(restAuthenticationService(),
				privilegeManagement.privilegeContextFactory(), data.systemDataView(), userStore);
		return new RestSessionLogic(new DefaultSessionLogic(delegate, sessionStore(), userStore, defaultSessionStore(),
				web.simpleTokenGenerator(), canImpersonate()));
	}

	@Bean
	protected CurrentSessionStore sessionStore() {
		return new ThreadLocalCurrentSessionStore();
	}

	@Bean
	protected Predicate<OperationUser> canImpersonate() {
		return new Predicate<OperationUser>() {

			@Override
			public boolean test(final OperationUser t) {
				return t.hasAdministratorPrivileges() || t.getAuthenticatedUser().isService()
						|| t.getAuthenticatedUser().isPrivileged();
			}

		};
	}

	@Bean
	protected SessionStore defaultSessionStore() {
		final CmdbuildConfiguration cmdbuildProperties = properties.cmdbuildProperties();
		return new DefaultSessionStore(new CachingStore<Session>() {

			private int duration = -1;
			private Cache<String, Session> cache;

			{
				cmdbuildProperties.addListener(new ChangeListener() {

					@Override
					public void changed() {
						setupCache();
					}

				});
				setupCache();
			}

			@Override
			protected Cache<String, Session> delegate() {
				synchronized (this) {
					return cache;
				}
			}

			private void setupCache() {
				synchronized (this) {
					final int value = cmdbuildProperties.getSessionTimeoutOrDefault();
					if (value != duration) {
						duration = value;
						final Cache<String, Session> old = cache;
						cache = CacheBuilder.newBuilder() //
								.expireAfterAccess((duration == 0) ? MAX_VALUE : duration, SECONDS) //
								.build();
						if (old != null) {
							for (final Entry<String, Session> entry : old.asMap().entrySet()) {
								cache.put(entry.getKey(), entry.getValue());
							}
						}
					}
				}
			}

		});
	}

	@Bean
	public GroupsLogic groupsLogic() {
		return new TransactionalGroupsLogic(
				new DefaultGroupsLogic(defaultAuthenticationService(), data.systemDataView(), userStore));
	}

}
