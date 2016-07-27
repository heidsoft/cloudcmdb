package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.api.mail.javax.mail.JavaxMailBasedMailApiFactory;
import org.cmdbuild.data.store.InMemoryStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.data.store.email.EmailAccountStorableConverter;
import org.cmdbuild.data.store.email.EmailConverter;
import org.cmdbuild.data.store.email.EmailStatusConverter;
import org.cmdbuild.data.store.email.EmailTemplateStorableConverter;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;
import org.cmdbuild.data.store.email.ExtendedEmailTemplateStore;
import org.cmdbuild.data.store.email.LookupStoreEmailStatusConverter;
import org.cmdbuild.data.store.email.StoreBasedEmailAccountFacade;
import org.cmdbuild.dms.ConfigurationAwareDmsService;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.email.ConfigurationAwareEmailAttachmentsLogic;
import org.cmdbuild.logic.email.DefaultEmailAccountLogic;
import org.cmdbuild.logic.email.DefaultEmailAttachmentsLogic;
import org.cmdbuild.logic.email.DefaultEmailLogic;
import org.cmdbuild.logic.email.DefaultEmailQueueLogic;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.DefaultSubjectHandler;
import org.cmdbuild.logic.email.EmailAccountLogic;
import org.cmdbuild.logic.email.EmailAttachmentsLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailQueueCommand;
import org.cmdbuild.logic.email.EmailQueueLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.SubjectHandler;
import org.cmdbuild.logic.email.TransactionalEmailTemplateLogic;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Email {

	@Autowired
	private Data data;

	@Autowired
	private Dms dms;

	@Autowired
	private DmsConfiguration dmsConfiguration;

	@Autowired
	private Properties properties;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private UserStore userStore;

	@Bean
	protected StorableConverter<EmailAccount> emailAccountConverter() {
		return new EmailAccountStorableConverter();
	}

	@Bean
	protected Store<EmailAccount> emailAccountStore() {
		return DataViewStore.<EmailAccount> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(emailAccountConverter()) //
				.build();
	}

	@Bean
	public EmailAccountFacade emailAccountFacade() {
		return new StoreBasedEmailAccountFacade(emailAccountStore());
	}

	@Bean
	public MailApiFactory mailApiFactory() {
		return new JavaxMailBasedMailApiFactory();
	}

	@Bean
	protected Store<org.cmdbuild.data.store.email.Email> emailStore() {
		return DataViewStore.<org.cmdbuild.data.store.email.Email> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(emailStorableConverter()) //
				.build();
	}

	@Bean
	protected StorableConverter<org.cmdbuild.data.store.email.Email> emailStorableConverter() {
		return new EmailConverter(emailStatusConverter());
	}

	@Bean
	protected EmailStatusConverter emailStatusConverter() {
		return new LookupStoreEmailStatusConverter(data.lookupStore());
	}

	@Bean
	public EmailServiceFactory emailServiceFactory() {
		return ConfigurableEmailServiceFactory.newInstance() //
				.withApiFactory(mailApiFactory()) //
				.withEmailAccountFacade(emailAccountFacade()) //
				.build();
	}

	@Bean
	public SubjectHandler subjectHandler() {
		return new DefaultSubjectHandler();
	}

	@Bean
	protected EmailTemplateStorableConverter emailTemplateStorableConverter() {
		return new EmailTemplateStorableConverter();
	}

	@Bean
	@Scope(PROTOTYPE)
	public EmailAttachmentsLogic emailAttachmentsLogic() {
		return new ConfigurationAwareEmailAttachmentsLogic( //
				new DefaultEmailAttachmentsLogic( //
						data.systemDataView(), //
						new ConfigurationAwareDmsService(dms.dmsService(), properties.dmsProperties()), //
						dms.documentCreatorFactory(), //
						userStore.getUser()), //
				dmsConfiguration);
	}

	@Bean
	public EmailLogic emailLogic() {
		return new DefaultEmailLogic(emailStore(), emailTemporaryStore(), emailStatusConverter(), emailAccountFacade());
	}

	@Bean
	protected Store<org.cmdbuild.data.store.email.Email> emailTemporaryStore() {
		return InMemoryStore.of(org.cmdbuild.data.store.email.Email.class);
	}

	@Bean
	public EmailTemplateLogic emailTemplateLogic() {
		return new TransactionalEmailTemplateLogic(new DefaultEmailTemplateLogic(templateStore(), emailAccountFacade()));
	}

	@Bean
	protected Store<ExtendedEmailTemplate> templateStore() {
		return ExtendedEmailTemplateStore.newInstance() //
				.withDataView(data.systemDataView()) //
				.withConverter(emailTemplateStorableConverter()) //
				.build();
	}

	@Bean
	public EmailAccountLogic emailAccountLogic() {
		return new DefaultEmailAccountLogic(emailAccountFacade());
	}

	@Bean
	public EmailQueueLogic emailQueue() {
		return new DefaultEmailQueueLogic(properties.emailProperties(), scheduler.defaultSchedulerService(),
				emailQueueCommand());
	}

	@Bean
	protected Command emailQueueCommand() {
		return new EmailQueueCommand(emailAccountFacade(), mailApiFactory(), emailLogic(), emailAttachmentsLogic(),
				subjectHandler());
	}

}
