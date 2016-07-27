package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.auth.acl.ForwardingPrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.logic.setup.DefaultModulesHandler;
import org.cmdbuild.logic.setup.SetupLogic;
import org.cmdbuild.logic.translation.DefaultSetupFacade;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.services.setup.PrivilegedModulesHandler;
import org.cmdbuild.services.setup.PropertiesModulesHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Setup {

	private static final String BIM_MODULE_NAME = "bim";

	@Autowired
	private LanguageStore languageStore;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Bean
	public SetupLogic setupLogic() {
		return new SetupLogic(privilegedModulesHandler());
	}

	@Bean
	// TODO: check!
	public SetupFacade setupFacade() {
		return new DefaultSetupFacade(setupLogic(), languageStore);
	}

	@Bean
	protected PrivilegedModulesHandler privilegedModulesHandler() {
		final PrivilegeContext context = new ForwardingPrivilegeContext() {

			@Override
			protected PrivilegeContext delegate() {
				return privilegeManagement.userPrivilegeContext();
			}

		};
		final PrivilegedModulesHandler privilegedModulesHandler = new PrivilegedModulesHandler(defaultModulesHandler(),
				context);
		privilegedModulesHandler.skipPrivileges(BIM_MODULE_NAME);
		return privilegedModulesHandler;
	}

	@Bean
	protected DefaultModulesHandler defaultModulesHandler() {
		return new DefaultModulesHandler(propertiesModulesHandler());
	}

	@Bean
	protected PropertiesModulesHandler propertiesModulesHandler() {
		return new PropertiesModulesHandler();
	}

}
