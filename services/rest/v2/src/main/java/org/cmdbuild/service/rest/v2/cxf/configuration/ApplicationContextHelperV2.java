package org.cmdbuild.service.rest.v2.cxf.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.config.GraphConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.NavigationTreeLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.RestSessionLogic;
import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.access.WebServiceDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.files.FileLogic;
import org.cmdbuild.logic.icon.IconsLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.workflow.WebserviceWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.localization.RequestHandler;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.workflow.LookupHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHelperV2 {

	@Autowired
	private ApplicationContext applicationContext;

	public AuthenticationLogic authenticationLogic() {
		return applicationContext.getBean(RestSessionLogic.class);
	}

	public CmdbuildConfiguration cmdbuildConfiguration() {
		return applicationContext.getBean(CmdbuildConfiguration.class);
	}

	public DmsLogic dmsLogic() {
		return applicationContext.getBean(PrivilegedDmsLogic.class);
	}

	public EmailLogic emailLogic() {
		return applicationContext.getBean(EmailLogic.class);
	}

	public EmailTemplateLogic emailTemplateLogic() {
		return applicationContext.getBean(EmailTemplateLogic.class);
	}

	public FileLogic fileLogic() {
		return applicationContext.getBean(FileLogic.class);
	}

	public FilesStore filesStore() {
		return applicationContext.getBean(FilesStore.class);
	}

	public GraphConfiguration graphConfiguration() {
		return applicationContext.getBean(GraphConfiguration.class);
	}

	public IconsLogic iconsLogic() {
		return applicationContext.getBean(IconsLogic.class);
	}

	public LookupHelper lookupHelper() {
		return applicationContext.getBean(LookupHelper.class);
	}

	public LookupLogic lookupLogic() {
		return applicationContext.getBean(LookupLogic.class);
	}

	public MenuLogic menuLogic() {
		return applicationContext.getBean(MenuLogic.class);
	}

	public MetadataStoreFactory metadataStoreFactory() {
		return applicationContext.getBean(MetadataStoreFactory.class);
	}

	public NavigationTreeLogic navigationTreeLogic() {
		return applicationContext.getBean(NavigationTreeLogic.class);
	}

	public ReportLogic reportLogic() {
		return applicationContext.getBean(ReportLogic.class);
	}

	public RequestHandler requestHandler() {
		return applicationContext.getBean(RequestHandler.class);
	}

	public SecurityLogic securityLogic() {
		return applicationContext.getBean(SecurityLogic.class);
	}

	public SessionLogic sessionLogic() {
		return applicationContext.getBean(RestSessionLogic.class);
	}

	public DataAccessLogic systemDataAccessLogic() {
		return applicationContext.getBean(SystemDataAccessLogicBuilder.class).build();
	}

	public CMDataView systemDataView() {
		return applicationContext.getBean("systemDataView", CMDataView.class);
	}

	public DataAccessLogic userDataAccessLogic() {
		return applicationContext.getBean(WebServiceDataAccessLogicBuilder.class).build();
	}

	public CMDataView userDataView() {
		return applicationContext.getBean("UserDataView", CMDataView.class);
	}

	public UserStore userStore() {
		return applicationContext.getBean(UserStore.class);
	}

	public WorkflowLogic userWorkflowLogic() {
		return applicationContext.getBean(WebserviceWorkflowLogicBuilder.class).build();
	}

}
