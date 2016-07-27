package org.cmdbuild.logic.email;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.data.store.Storables.storableOf;
import static org.cmdbuild.data.store.Stores.nullOnNotFoundRead;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.DefaultEmailTemplate;
import org.cmdbuild.data.store.email.DefaultExtendedEmailTemplate;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class DefaultEmailTemplateLogic implements EmailTemplateLogic {

	private static final Marker marker = MarkerFactory.getMarker(DefaultEmailTemplateLogic.class.getName());

	private static class TemplateWrapper implements Template {

		private final ExtendedEmailTemplate delegate;
		private final EmailAccountFacade accountStoreFacade;

		public TemplateWrapper(final ExtendedEmailTemplate delegate, final EmailAccountFacade accountStoreFacade) {
			this.delegate = delegate;
			this.accountStoreFacade = accountStoreFacade;
		}

		@Override
		public Long getId() {
			return delegate.getId();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
		}

		@Override
		public String getFrom() {
			return delegate.getFrom();
		}

		@Override
		public String getTo() {
			return delegate.getTo();
		}

		@Override
		public String getCc() {
			return delegate.getCc();
		}

		@Override
		public String getBcc() {
			return delegate.getBcc();
		}

		@Override
		public String getSubject() {
			return delegate.getSubject();
		}

		@Override
		public String getBody() {
			return delegate.getBody();
		}

		@Override
		public String getAccount() {
			return accountOf(delegate);
		}

		private String accountOf(final ExtendedEmailTemplate input) {
			if (input.getAccount() != null) {
				final Optional<EmailAccount> account = accountStoreFacade.fromId(input.getAccount());
				return account.isPresent() ? account.get().getName() : null;
			}
			return null;
		};

		@Override
		public boolean isKeepSynchronization() {
			return delegate.isKeepSynchronization();
		}

		@Override
		public boolean isPromptSynchronization() {
			return delegate.isPromptSynchronization();
		}

		@Override
		public long getDelay() {
			return delegate.getDelay();
		}

		@Override
		public Map<String, String> getVariables() {
			return delegate.getVariables();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class EmailTemplate_to_Template implements Function<ExtendedEmailTemplate, Template> {

		private final EmailAccountFacade emailAccountFacade;

		public EmailTemplate_to_Template(final EmailAccountFacade emailAccountFacade) {
			this.emailAccountFacade = emailAccountFacade;
		}

		@Override
		public Template apply(final ExtendedEmailTemplate input) {
			return new TemplateWrapper(input, emailAccountFacade);
		};

	};

	private static class Template_To_EmailTemplate implements Function<Template, ExtendedEmailTemplate> {

		private final Store<EmailAccount> nullOnNotFoundStore;

		public Template_To_EmailTemplate(final Store<EmailAccount> store) {
			this.nullOnNotFoundStore = nullOnNotFoundRead(store);
		}

		@Override
		public ExtendedEmailTemplate apply(final Template input) {
			return DefaultExtendedEmailTemplate.newInstance() //
					.withDelegate(DefaultEmailTemplate.newInstance() //
							.withId(input.getId()) //
							.withName(input.getName()) //
							.withDescription(input.getDescription()) //
							.withFrom(input.getFrom()) //
							.withTo(input.getTo()) //
							.withCc(input.getCc()) //
							.withBcc(input.getBcc()) //
							.withSubject(input.getSubject()) //
							.withBody(input.getBody()) //
							.withAccount(accountIdOf(input)) //
							.withKeepSynchronization(input.isKeepSynchronization()) //
							.withPromptSynchronization(input.isPromptSynchronization()) //
							.withDelay(input.getDelay()) //
							.build()) //
					.withVariables(input.getVariables()) //
					.build();
		}

		private Long accountIdOf(final Template input) {
			final EmailAccount account = nullOnNotFoundStore.read(storableOf(input.getAccount()));
			return (account == null) ? null : account.getId();
		};

	};

	private static Function<? super ExtendedEmailTemplate, String> TO_NAME = new Function<ExtendedEmailTemplate, String>() {

		@Override
		public String apply(final ExtendedEmailTemplate input) {
			return input.getName();
		}

	};

	private final Store<ExtendedEmailTemplate> store;
	private final EmailTemplate_to_Template emailTemplate_to_Template;
	private final Template_To_EmailTemplate template_To_EmailTemplate;

	public DefaultEmailTemplateLogic(final Store<ExtendedEmailTemplate> store,
			final EmailAccountFacade accountStoreFacade) {
		this.store = store;
		this.emailTemplate_to_Template = new EmailTemplate_to_Template(accountStoreFacade);
		this.template_To_EmailTemplate = new Template_To_EmailTemplate(accountStoreFacade);
	}

	@Override
	public Iterable<Template> readAll() {
		logger.info(marker, "reading all templates");
		return from(store.readAll()) //
				.transform(emailTemplate_to_Template);
	}

	@Override
	public Template read(final String name) {
		logger.info(marker, "reading template '{}'", name);
		assureOneOnlyWithName(name);
		final ExtendedEmailTemplate template = DefaultExtendedEmailTemplate.newInstance() //
				.withDelegate(DefaultEmailTemplate.newInstance() //
						.withName(name) //
						.build()) //
				.build();
		final ExtendedEmailTemplate readed = store.read(template);
		return emailTemplate_to_Template.apply(readed);
	}

	@Override
	public Long create(final Template template) {
		logger.info(marker, "creating template '{}'", template);
		assureNoOneWithName(template.getName());
		Validate.isTrue(isNotBlank(template.getDescription()), "invalid description");
		final Storable created = store.create(template_To_EmailTemplate.apply(template));
		final EmailTemplate readed = store.read(created);
		return readed.getId();
	}

	@Override
	public void update(final Template template) {
		logger.info(marker, "updating template '{}'", template);
		assureOneOnlyWithName(template.getName());
		Validate.isTrue(isNotBlank(template.getDescription()), "invalid description");
		store.update(template_To_EmailTemplate.apply(template));
	}

	@Override
	public void delete(final String name) {
		logger.info(marker, "deleting template '{}'", name);
		assureOneOnlyWithName(name);
		final EmailTemplate emailTemplate = DefaultEmailTemplate.newInstance() //
				.withName(name) //
				.build();
		store.delete(emailTemplate);
	}

	private void assureNoOneWithName(final String name) {
		final boolean existing = from(store.readAll()) //
				.transform(TO_NAME) //
				.contains(name);
		Validate.isTrue(!existing, "already existing element");
	}

	private void assureOneOnlyWithName(final String name) {
		final int count = from(store.readAll()) //
				.transform(TO_NAME) //
				.filter(equalTo(name)) //
				.size();
		Validate.isTrue(!(count == 0), "element not found");
		Validate.isTrue(!(count > 1), "multiple elements found");
	}

}
