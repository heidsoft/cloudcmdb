package org.cmdbuild.service.rest.v2.cxf.configuration;

import static com.google.common.reflect.Reflection.newProxy;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler.Announceable;
import org.cmdbuild.service.rest.v2.AttachmentsConfiguration;
import org.cmdbuild.service.rest.v2.CardAttachments;
import org.cmdbuild.service.rest.v2.CardEmails;
import org.cmdbuild.service.rest.v2.Cards;
import org.cmdbuild.service.rest.v2.ClassAttributes;
import org.cmdbuild.service.rest.v2.ClassPrivileges;
import org.cmdbuild.service.rest.v2.Classes;
import org.cmdbuild.service.rest.v2.Cql;
import org.cmdbuild.service.rest.v2.DomainAttributes;
import org.cmdbuild.service.rest.v2.DomainTrees;
import org.cmdbuild.service.rest.v2.Domains;
import org.cmdbuild.service.rest.v2.EmailTemplates;
import org.cmdbuild.service.rest.v2.FileStores;
import org.cmdbuild.service.rest.v2.Functions;
import org.cmdbuild.service.rest.v2.GraphConfiguration;
import org.cmdbuild.service.rest.v2.Icons;
import org.cmdbuild.service.rest.v2.Impersonate;
import org.cmdbuild.service.rest.v2.LookupTypeValues;
import org.cmdbuild.service.rest.v2.LookupTypes;
import org.cmdbuild.service.rest.v2.Menu;
import org.cmdbuild.service.rest.v2.ProcessAttributes;
import org.cmdbuild.service.rest.v2.ProcessInstanceActivities;
import org.cmdbuild.service.rest.v2.ProcessInstanceAttachments;
import org.cmdbuild.service.rest.v2.ProcessInstanceEmails;
import org.cmdbuild.service.rest.v2.ProcessInstancePrivileges;
import org.cmdbuild.service.rest.v2.ProcessInstances;
import org.cmdbuild.service.rest.v2.ProcessStartActivities;
import org.cmdbuild.service.rest.v2.Processes;
import org.cmdbuild.service.rest.v2.ProcessesConfiguration;
import org.cmdbuild.service.rest.v2.Relations;
import org.cmdbuild.service.rest.v2.Reports;
import org.cmdbuild.service.rest.v2.Sessions;
import org.cmdbuild.service.rest.v2.cxf.AttachmentsHelper;
import org.cmdbuild.service.rest.v2.cxf.AttachmentsManagement;
import org.cmdbuild.service.rest.v2.cxf.CxfAttachmentsConfiguration;
import org.cmdbuild.service.rest.v2.cxf.CxfCardAttachments;
import org.cmdbuild.service.rest.v2.cxf.CxfCardEmails;
import org.cmdbuild.service.rest.v2.cxf.CxfCards;
import org.cmdbuild.service.rest.v2.cxf.CxfClassAttributes;
import org.cmdbuild.service.rest.v2.cxf.CxfClassPrivileges;
import org.cmdbuild.service.rest.v2.cxf.CxfClasses;
import org.cmdbuild.service.rest.v2.cxf.CxfCql;
import org.cmdbuild.service.rest.v2.cxf.CxfDomainAttributes;
import org.cmdbuild.service.rest.v2.cxf.CxfDomainTrees;
import org.cmdbuild.service.rest.v2.cxf.CxfDomains;
import org.cmdbuild.service.rest.v2.cxf.CxfEmailTemplates;
import org.cmdbuild.service.rest.v2.cxf.CxfFileStores;
import org.cmdbuild.service.rest.v2.cxf.CxfFunctions;
import org.cmdbuild.service.rest.v2.cxf.CxfGraphConfiguration;
import org.cmdbuild.service.rest.v2.cxf.CxfIcons;
import org.cmdbuild.service.rest.v2.cxf.CxfImpersonate;
import org.cmdbuild.service.rest.v2.cxf.CxfLookupTypeValues;
import org.cmdbuild.service.rest.v2.cxf.CxfLookupTypes;
import org.cmdbuild.service.rest.v2.cxf.CxfMenu;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessAttributes;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstanceActivities;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstanceAttachments;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstanceEmails;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstancePrivileges;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstances;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessStartActivities;
import org.cmdbuild.service.rest.v2.cxf.CxfProcesses;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessesConfiguration;
import org.cmdbuild.service.rest.v2.cxf.CxfRelations;
import org.cmdbuild.service.rest.v2.cxf.CxfReports;
import org.cmdbuild.service.rest.v2.cxf.CxfSessions;
import org.cmdbuild.service.rest.v2.cxf.DefaultEncoding;
import org.cmdbuild.service.rest.v2.cxf.DefaultIdGenerator;
import org.cmdbuild.service.rest.v2.cxf.DefaultProcessStatusHelper;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.cxf.HeaderResponseHandler;
import org.cmdbuild.service.rest.v2.cxf.IdGenerator;
import org.cmdbuild.service.rest.v2.cxf.ProcessStatusHelper;
import org.cmdbuild.service.rest.v2.cxf.TranslatingAttachmentsHelper;
import org.cmdbuild.service.rest.v2.cxf.TranslatingAttachmentsHelper.Encoding;
import org.cmdbuild.service.rest.v2.cxf.WebApplicationExceptionErrorHandler;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

@Configuration
public class ServicesV2 implements LoggingSupport {

	@Autowired
	private ApplicationContextHelperV2 helper;

	@Bean
	public AttachmentsConfiguration v2_attachmentsConfiguration() {
		final CxfAttachmentsConfiguration service = new CxfAttachmentsConfiguration(helper.dmsLogic());
		return proxy(AttachmentsConfiguration.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public CardAttachments v2_cardAttachments() {
		final CxfCardAttachments service = new CxfCardAttachments(v2_errorHandler(), helper.systemDataAccessLogic(),
				v2_attachmentsHelper());
		return proxy(CardAttachments.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public CardEmails v2_cardEmails() {
		final CxfCardEmails service = new CxfCardEmails(v2_errorHandler(), helper.userDataAccessLogic(),
				helper.emailLogic(), v2_idGenerator());
		return proxy(CardEmails.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Cards v2_cards() {
		final CxfCards service = new CxfCards(v2_errorHandler(), helper.userDataAccessLogic());
		return proxy(Cards.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ClassAttributes v2_classAttributes() {
		final CxfClassAttributes service = new CxfClassAttributes(v2_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(ClassAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ClassPrivileges v2_classPrivileges() {
		final CxfClassPrivileges service = new CxfClassPrivileges(v2_errorHandler(), helper.authenticationLogic(),
				helper.securityLogic(), helper.userDataAccessLogic());
		return proxy(ClassPrivileges.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Classes v2_classes() {
		final CxfClasses service = new CxfClasses(v2_errorHandler(), helper.userDataAccessLogic());
		return proxy(Classes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Cql v2_cql() {
		final CxfCql service = new CxfCql(helper.userDataAccessLogic());
		return proxy(Cql.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public DomainAttributes v2_domainAttributes() {
		final CxfDomainAttributes service = new CxfDomainAttributes(v2_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(DomainAttributes.class, service);
	}

	@Bean
	public EmailTemplates v2_emailTemplates() {
		final CxfEmailTemplates service = new CxfEmailTemplates(helper.emailTemplateLogic());
		return proxy(EmailTemplates.class, service);
	}

	@Bean
	public Icons v2_icons() {
		final CxfIcons service = new CxfIcons(v2_errorHandler(), helper.iconsLogic(),
				new CxfIcons.ConverterImpl(v2_errorHandler()));
		return proxy(Icons.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Relations v2_relations() {
		final CxfRelations service = new CxfRelations(v2_errorHandler(), helper.userDataAccessLogic());
		return proxy(Relations.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Domains v2_domains() {
		final CxfDomains service = new CxfDomains(v2_errorHandler(), helper.userDataAccessLogic());
		return proxy(Domains.class, service);
	}

	@Bean
	@Scope()
	public DomainTrees v2_domainTrees() {
		final CxfDomainTrees service = new CxfDomainTrees(v2_errorHandler(), helper.navigationTreeLogic());
		return proxy(DomainTrees.class, service);
	}

	@Bean
	public Impersonate v2_impersonate() {
		final CxfImpersonate service = new CxfImpersonate(v2_errorHandler(), helper.sessionLogic(),
				v2_operationUserAllowed());
		return proxy(Impersonate.class, service);
	}

	@Bean
	protected Predicate<OperationUser> v2_operationUserAllowed() {
		return new Predicate<OperationUser>() {

			@Override
			public boolean apply(final OperationUser input) {
				final AuthenticatedUser authenticatedUser = input.getAuthenticatedUser();
				return input.hasAdministratorPrivileges() || authenticatedUser.isService()
						|| authenticatedUser.isPrivileged();
			}

		};
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypes v2_lookupTypes() {
		final CxfLookupTypes service = new CxfLookupTypes(v2_errorHandler(), helper.lookupLogic());
		return proxy(LookupTypes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypeValues v2_lookupTypeValues() {
		final CxfLookupTypeValues service = new CxfLookupTypeValues(v2_errorHandler(), helper.lookupLogic());
		return proxy(LookupTypeValues.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Menu v2_menu() {
		final CxfMenu service = new CxfMenu(currentGroupNameSupplier(), helper.menuLogic(), helper.systemDataView());
		return proxy(Menu.class, service);
	}

	private Supplier<String> currentGroupNameSupplier() {
		return new Supplier<String>() {

			@Override
			public String get() {
				return helper.userStore().getUser().getPreferredGroup().getName();
			}

		};
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessAttributes v2_processAttributes() {
		final CxfProcessAttributes service = new CxfProcessAttributes(v2_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(ProcessAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Processes v2_processes() {
		final CxfProcesses service = new CxfProcesses(v2_errorHandler(), helper.userWorkflowLogic(),
				v2_processStatusHelper(), v2_idGenerator());
		return proxy(Processes.class, service);
	}

	@Bean
	public ProcessesConfiguration v2_processesConfiguration() {
		final CxfProcessesConfiguration service = new CxfProcessesConfiguration(v2_processStatusHelper());
		return proxy(ProcessesConfiguration.class, service);
	}

	@Bean
	protected ProcessStatusHelper v2_processStatusHelper() {
		return new DefaultProcessStatusHelper(helper.lookupHelper());
	}

	@Bean
	protected IdGenerator v2_idGenerator() {
		return new DefaultIdGenerator();
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstanceActivities v2_processInstanceActivities() {
		final CxfProcessInstanceActivities service = new CxfProcessInstanceActivities(v2_errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessInstanceActivities.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstanceAttachments v2_processInstanceAttachments() {
		final CxfProcessInstanceAttachments service = new CxfProcessInstanceAttachments(v2_errorHandler(),
				helper.userWorkflowLogic(), v2_attachmentsHelper());
		return proxy(ProcessInstanceAttachments.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstanceEmails v2_processInstanceEmails() {
		final CxfProcessInstanceEmails service = new CxfProcessInstanceEmails(v2_errorHandler(),
				helper.userWorkflowLogic(), helper.emailLogic(), v2_idGenerator());
		return proxy(ProcessInstanceEmails.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstancePrivileges v2_processInstancePrivileges() {
		final CxfProcessInstancePrivileges service = new CxfProcessInstancePrivileges(v2_errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessInstancePrivileges.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstances v2_processInstances() {
		final CxfProcessInstances service = new CxfProcessInstances(v2_errorHandler(), helper.userWorkflowLogic(),
				helper.lookupHelper());
		return proxy(ProcessInstances.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessStartActivities v2_processStartActivities() {
		final CxfProcessStartActivities service = new CxfProcessStartActivities(v2_errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessStartActivities.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Functions v2_functions() {
		final CxfFunctions service = new CxfFunctions(v2_errorHandler(), helper.userDataView());
		return proxy(Functions.class, service);
	}

	@Bean
	public Sessions v2_sessions() {
		final CxfSessions service = new CxfSessions(v2_errorHandler(), helper.sessionLogic());
		return proxy(Sessions.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	protected AttachmentsHelper v2_attachmentsHelper() {
		return new TranslatingAttachmentsHelper(new AttachmentsManagement(helper.dmsLogic(), helper.userStore()),
				v2_encoding());
	}

	@Bean
	protected Encoding v2_encoding() {
		return new DefaultEncoding();
	}

	@Bean
	public Reports v2_reports() {
		final CxfReports service = new CxfReports(v2_errorHandler(), helper.reportLogic(), helper.systemDataView(),
				helper.lookupLogic());
		return proxy(Reports.class, service);
	}

	private <T> T proxy(final Class<T> type, final T service) {
		final InvocationHandler serviceWithAnnounces = AnnouncingInvocationHandler.of(service, v2_announceable());
		return newProxy(type, serviceWithAnnounces);
	}

	@Bean
	protected Announceable v2_announceable() {
		return new LoggingAnnounceable();
	}

	private static final class LoggingAnnounceable implements Announceable {

		@Override
		public void announce(final Method method, final Object[] args) {
			logger.info("invoking method '{}' with arguments '{}'", method, args);
		}

	}

	@Bean
	protected ErrorHandler v2_errorHandler() {
		return new WebApplicationExceptionErrorHandler();
	}

	@Bean
	public HeaderResponseHandler v2_headerResponseHandler() {
		return new HeaderResponseHandler();
	}

	@Bean
	public GraphConfiguration v2_graphConfiguration() {
		final CxfGraphConfiguration service = new CxfGraphConfiguration(helper.graphConfiguration());
		return proxy(GraphConfiguration.class, service);
	}

	@Bean
	public FileStores v2_fileStores() {
		final CxfFileStores service = new CxfFileStores(v2_errorHandler(), helper.fileLogic());
		return proxy(FileStores.class, service);
	}

}
