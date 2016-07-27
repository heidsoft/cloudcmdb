package org.cmdbuild.services.email;

import static com.google.common.base.Suppliers.ofInstance;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public class ConfigurableEmailServiceFactory implements EmailServiceFactory {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ConfigurableEmailServiceFactory> {

		private MailApiFactory apiFactory;
		private EmailAccountFacade emailAccountFacade;

		private Builder() {
			// use factory method
		}

		@Override
		public ConfigurableEmailServiceFactory build() {
			validate();
			return new ConfigurableEmailServiceFactory(this);
		}

		private void validate() {
			Validate.notNull(apiFactory, "missing '%s'", MailApiFactory.class);
			Validate.notNull(emailAccountFacade, "missing '%s'", EmailAccountFacade.class);
		};

		public Builder withApiFactory(final MailApiFactory apiFactory) {
			this.apiFactory = apiFactory;
			return this;
		}

		public Builder withEmailAccountFacade(final EmailAccountFacade emailAccountFacade) {
			this.emailAccountFacade = emailAccountFacade;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final MailApiFactory apiFactory;
	private final EmailAccountFacade emailAccountFacade;

	private ConfigurableEmailServiceFactory(final Builder builder) {
		this.apiFactory = builder.apiFactory;
		this.emailAccountFacade = builder.emailAccountFacade;
	}

	@Override
	public EmailService create() {
		final Optional<EmailAccount> account = emailAccountFacade.defaultAccount();
		return create(ofInstance(account.get()));
	}

	@Override
	public EmailService create(final Supplier<EmailAccount> emailAccountSupplier) {
		Validate.notNull(emailAccountSupplier, "missing '%s'", EmailAccount.class);
		return new DefaultEmailService(emailAccountSupplier, apiFactory);
	}

}
