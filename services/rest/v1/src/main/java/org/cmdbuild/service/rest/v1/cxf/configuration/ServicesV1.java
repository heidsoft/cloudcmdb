package org.cmdbuild.service.rest.v1.cxf.configuration;

import static com.google.common.reflect.Reflection.newProxy;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler.Announceable;
import org.cmdbuild.service.rest.v1.AttachmentsConfiguration;
import org.cmdbuild.service.rest.v1.Cards;
import org.cmdbuild.service.rest.v1.ClassAttributes;
import org.cmdbuild.service.rest.v1.ClassPrivileges;
import org.cmdbuild.service.rest.v1.Classes;
import org.cmdbuild.service.rest.v1.DomainAttributes;
import org.cmdbuild.service.rest.v1.Domains;
import org.cmdbuild.service.rest.v1.Impersonate;
import org.cmdbuild.service.rest.v1.LookupTypeValues;
import org.cmdbuild.service.rest.v1.LookupTypes;
import org.cmdbuild.service.rest.v1.Menu;
import org.cmdbuild.service.rest.v1.ProcessAttributes;
import org.cmdbuild.service.rest.v1.ProcessInstanceActivities;
import org.cmdbuild.service.rest.v1.ProcessInstances;
import org.cmdbuild.service.rest.v1.ProcessStartActivities;
import org.cmdbuild.service.rest.v1.Processes;
import org.cmdbuild.service.rest.v1.ProcessesConfiguration;
import org.cmdbuild.service.rest.v1.Relations;
import org.cmdbuild.service.rest.v1.Sessions;
import org.cmdbuild.service.rest.v1.cxf.AllInOneCardAttachments;
import org.cmdbuild.service.rest.v1.cxf.AllInOneProcessInstanceAttachments;
import org.cmdbuild.service.rest.v1.cxf.AttachmentsHelper;
import org.cmdbuild.service.rest.v1.cxf.AttachmentsManagement;
import org.cmdbuild.service.rest.v1.cxf.CxfAttachmentsConfiguration;
import org.cmdbuild.service.rest.v1.cxf.CxfCardAttachments;
import org.cmdbuild.service.rest.v1.cxf.CxfCards;
import org.cmdbuild.service.rest.v1.cxf.CxfClassAttributes;
import org.cmdbuild.service.rest.v1.cxf.CxfClassPrivileges;
import org.cmdbuild.service.rest.v1.cxf.CxfClasses;
import org.cmdbuild.service.rest.v1.cxf.CxfDomainAttributes;
import org.cmdbuild.service.rest.v1.cxf.CxfDomains;
import org.cmdbuild.service.rest.v1.cxf.CxfImpersonate;
import org.cmdbuild.service.rest.v1.cxf.CxfLookupTypeValues;
import org.cmdbuild.service.rest.v1.cxf.CxfLookupTypes;
import org.cmdbuild.service.rest.v1.cxf.CxfMenu;
import org.cmdbuild.service.rest.v1.cxf.CxfProcessAttributes;
import org.cmdbuild.service.rest.v1.cxf.CxfProcessInstanceActivities;
import org.cmdbuild.service.rest.v1.cxf.CxfProcessInstanceAttachments;
import org.cmdbuild.service.rest.v1.cxf.CxfProcessInstances;
import org.cmdbuild.service.rest.v1.cxf.CxfProcessStartActivities;
import org.cmdbuild.service.rest.v1.cxf.CxfProcesses;
import org.cmdbuild.service.rest.v1.cxf.CxfProcessesConfiguration;
import org.cmdbuild.service.rest.v1.cxf.CxfRelations;
import org.cmdbuild.service.rest.v1.cxf.CxfSessions;
import org.cmdbuild.service.rest.v1.cxf.DefaultEncoding;
import org.cmdbuild.service.rest.v1.cxf.DefaultProcessStatusHelper;
import org.cmdbuild.service.rest.v1.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v1.cxf.HeaderResponseHandler;
import org.cmdbuild.service.rest.v1.cxf.ProcessStatusHelper;
import org.cmdbuild.service.rest.v1.cxf.TranslatingAttachmentsHelper;
import org.cmdbuild.service.rest.v1.cxf.TranslatingAttachmentsHelper.Encoding;
import org.cmdbuild.service.rest.v1.cxf.WebApplicationExceptionErrorHandler;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

@Configuration
public class ServicesV1 implements LoggingSupport {

	@Autowired
	private ApplicationContextHelperV1 helper;

	@Bean
	public AttachmentsConfiguration v1_attachmentsConfiguration() {
		final CxfAttachmentsConfiguration service = new CxfAttachmentsConfiguration(helper.dmsLogic());
		return proxy(AttachmentsConfiguration.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public AllInOneCardAttachments v1_cardAttachments() {
		final CxfCardAttachments service = new CxfCardAttachments(v1_errorHandler(), helper.systemDataAccessLogic(),
				v1_attachmentsHelper());
		return proxy(AllInOneCardAttachments.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Cards v1_cards() {
		final CxfCards service = new CxfCards(v1_errorHandler(), helper.userDataAccessLogic());
		return proxy(Cards.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ClassAttributes v1_classAttributes() {
		final CxfClassAttributes service = new CxfClassAttributes(v1_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(ClassAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ClassPrivileges v1_classPrivileges() {
		final CxfClassPrivileges service = new CxfClassPrivileges(v1_errorHandler(), helper.authenticationLogic(),
				helper.securityLogic(), helper.userDataAccessLogic());
		return proxy(ClassPrivileges.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Classes v1_classes() {
		final CxfClasses service = new CxfClasses(v1_errorHandler(), helper.userDataAccessLogic());
		return proxy(Classes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public DomainAttributes v1_domainAttributes() {
		final CxfDomainAttributes service = new CxfDomainAttributes(v1_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(DomainAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Relations v1_relations() {
		final CxfRelations service = new CxfRelations(v1_errorHandler(), helper.userDataAccessLogic());
		return proxy(Relations.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Domains v1_domains() {
		final CxfDomains service = new CxfDomains(v1_errorHandler(), helper.userDataAccessLogic());
		return proxy(Domains.class, service);
	}

	@Bean
	public Impersonate v1_impersonate() {
		final CxfImpersonate service = new CxfImpersonate(v1_errorHandler(), helper.sessionLogic(),
				v1_operationUserAllowed());
		return proxy(Impersonate.class, service);
	}

	@Bean
	protected Predicate<OperationUser> v1_operationUserAllowed() {
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
	public LookupTypes v1_lookupTypes() {
		final CxfLookupTypes service = new CxfLookupTypes(v1_errorHandler(), helper.lookupLogic());
		return proxy(LookupTypes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypeValues v1_lookupTypeValues() {
		final CxfLookupTypeValues service = new CxfLookupTypeValues(v1_errorHandler(), helper.lookupLogic());
		return proxy(LookupTypeValues.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Menu v1_menu() {
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
	public ProcessAttributes v1_processAttributes() {
		final CxfProcessAttributes service = new CxfProcessAttributes(v1_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(ProcessAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Processes v1_processes() {
		final CxfProcesses service = new CxfProcesses(v1_errorHandler(), helper.userWorkflowLogic(),
				v1_processStatusHelper());
		return proxy(Processes.class, service);
	}

	@Bean
	public ProcessesConfiguration v1_processesConfiguration() {
		final CxfProcessesConfiguration service = new CxfProcessesConfiguration(v1_processStatusHelper());
		return proxy(ProcessesConfiguration.class, service);
	}

	@Bean
	protected ProcessStatusHelper v1_processStatusHelper() {
		return new DefaultProcessStatusHelper(helper.lookupHelper());
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstanceActivities v1_processInstanceActivities() {
		final CxfProcessInstanceActivities service = new CxfProcessInstanceActivities(v1_errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessInstanceActivities.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public AllInOneProcessInstanceAttachments v1_processInstanceAttachments() {
		final CxfProcessInstanceAttachments service = new CxfProcessInstanceAttachments(v1_errorHandler(),
				helper.userWorkflowLogic(), v1_attachmentsHelper());
		return proxy(AllInOneProcessInstanceAttachments.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstances v1_processInstances() {
		final CxfProcessInstances service = new CxfProcessInstances(v1_errorHandler(), helper.userWorkflowLogic(),
				helper.lookupHelper());
		return proxy(ProcessInstances.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessStartActivities v1_processStartActivities() {
		final CxfProcessStartActivities service = new CxfProcessStartActivities(v1_errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessStartActivities.class, service);
	}

	@Bean
	public Sessions v1_sessions() {
		final CxfSessions service = new CxfSessions(v1_errorHandler(), helper.sessionLogic());
		return proxy(Sessions.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	protected AttachmentsHelper v1_attachmentsHelper() {
		return new TranslatingAttachmentsHelper(new AttachmentsManagement(helper.dmsLogic(), helper.userStore()),
				v1_encoding());
	}

	@Bean
	protected Encoding v1_encoding() {
		return new DefaultEncoding();
	}

	private <T> T proxy(final Class<T> type, final T service) {
		final InvocationHandler serviceWithAnnounces = AnnouncingInvocationHandler.of(service, v1_announceable());
		return newProxy(type, serviceWithAnnounces);
	}

	@Bean
	protected Announceable v1_announceable() {
		return new LoggingAnnounceable();
	}

	private static final class LoggingAnnounceable implements Announceable {

		@Override
		public void announce(final Method method, final Object[] args) {
			logger.info("invoking method '{}' with arguments '{}'", method, args);
		}

	}

	@Bean
	protected ErrorHandler v1_errorHandler() {
		return new WebApplicationExceptionErrorHandler();
	}

	@Bean
	public HeaderResponseHandler v1_headerResponseHandler() {
		return new HeaderResponseHandler();
	}

}
