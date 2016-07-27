package org.cmdbuild.services.email;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.unmodifiableIterable;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.reflect.Reflection.newProxy;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.joda.time.DateTime.now;

import java.util.Collection;

import javax.activation.DataHandler;

import org.cmdbuild.common.api.mail.FetchedMail;
import org.cmdbuild.common.api.mail.GetMail;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.api.mail.MailException;
import org.cmdbuild.common.api.mail.SendableNewMail;
import org.cmdbuild.data.store.email.EmailAccount;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

public class DefaultEmailService implements EmailService {

	private static class MailApiSupplier implements Supplier<MailApi> {

		private final Supplier<EmailAccount> emailAccountSupplier;
		private final MailApiFactory mailApiFactory;

		public MailApiSupplier(final Supplier<EmailAccount> emailAccountSupplier, final MailApiFactory mailApiFactory) {
			this.emailAccountSupplier = emailAccountSupplier;
			this.mailApiFactory = mailApiFactory;
		}

		@Override
		public MailApi get() {
			final EmailAccount account = emailAccountSupplier.get();
			logger.trace("configuring mail API with configuration:", account);
			return mailApiFactory.create(AllConfigurationWrapper.of(account));
		}

	}

	private static class GetMailAdapter extends ForwardingEmail {

		private static final Email unsupported = newProxy(Email.class, unsupported("method not supported"));

		private final DateTime OBJECT_CREATION_TIME = now();

		private final GetMail delegate;
		private final String account;

		public GetMailAdapter(final GetMail delegate, final String account) {
			this.delegate = delegate;
			this.account = account;
		}

		@Override
		protected Email delegate() {
			return unsupported;
		}

		@Override
		public DateTime getDate() {
			return OBJECT_CREATION_TIME;
		}

		@Override
		public String getFromAddress() {
			return delegate.getFrom();
		}

		@Override
		public Iterable<String> getToAddresses() {
			return delegate.getTos();
		}

		@Override
		public Iterable<String> getCcAddresses() {
			return delegate.getCcs();
		}

		@Override
		public Iterable<String> getBccAddresses() {
			return NO_ADDRESSES;
		}

		@Override
		public String getSubject() {
			return delegate.getSubject();
		}

		@Override
		public String getContent() {
			return delegate.getContent();
		}

		@Override
		public Iterable<org.cmdbuild.services.email.Attachment> getAttachments() {
			return from(delegate.getAttachments()) //
					.transform(new Function<GetMail.Attachment, Attachment>() {

						@Override
						public Attachment apply(final GetMail.Attachment input) {
							return new AttachmentAdapter(input);
						}

					});
		}

		@Override
		public String getAccount() {
			return account;
		}

		@Override
		public long getDelay() {
			return 0;
		}

	}

	private static class AttachmentAdapter extends ForwardingAttachment {

		private static final Attachment unsupported = newProxy(Attachment.class, unsupported("method not supported"));

		private final GetMail.Attachment delegate;

		public AttachmentAdapter(final GetMail.Attachment delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Attachment delegate() {
			return unsupported;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public DataHandler getDataHandler() {
			return delegate.getDataHandler();
		}

	}

	private static class CollectingEmailCallbackHandler implements EmailCallbackHandler {

		private final Collection<Email> emails = newArrayList();

		@Override
		public void handle(final Email email) {
			emails.add(email);
		}

		public Iterable<Email> getEmails() {
			return emails;
		}

	}

	private static final Iterable<String> NO_ADDRESSES = emptyList();
	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	private final Supplier<EmailAccount> accountSupplier;
	private final Supplier<MailApi> apiSupplier;

	DefaultEmailService(final Supplier<EmailAccount> emailAccountSupplier, final MailApiFactory mailApiFactory) {
		this.accountSupplier = emailAccountSupplier;
		this.apiSupplier = new MailApiSupplier(emailAccountSupplier, mailApiFactory);
	}

	@Override
	public void send(final Email email) throws EmailServiceException {
		try {
			logger.info("sending email '{}'", email);
			final SendableNewMail newMail = apiSupplier.get().newMail() //
					.withFrom(defaultIfBlank(email.getFromAddress(), accountSupplier.get().getAddress())) //
					.withTo(defaultIfNull(email.getToAddresses(), NO_ADDRESSES)) //
					.withCc(defaultIfNull(email.getCcAddresses(), NO_ADDRESSES)) //
					.withBcc(defaultIfNull(email.getBccAddresses(), NO_ADDRESSES)) //
					.withSubject(email.getSubject()) //
					.withContent(email.getContent()) //
					.withContentType(CONTENT_TYPE);
			for (final Attachment attachment : email.getAttachments()) {
				newMail.withAttachment(attachment.getDataHandler(), attachment.getName());
			}
			newMail.send();
		} catch (final MailException e) {
			logger.error("error sending email", e);
			throw EmailServiceException.send(e);
		}
	}

	@Override
	public synchronized Iterable<Email> receive(final Folders folders) throws EmailServiceException {
		logger.info("receiving emails");
		final CollectingEmailCallbackHandler callbackHandler = new CollectingEmailCallbackHandler();
		receive(folders, callbackHandler);
		return unmodifiableIterable(callbackHandler.getEmails());
	}

	@Override
	public synchronized void receive(final Folders folders, final EmailCallbackHandler callback)
			throws EmailServiceException {
		logger.info("receiving emails");
		/**
		 * Business rule: Consider the configuration of the IMAP Server as check
		 * to sync the e-mails. So don't try to reach always the server but only
		 * if configured
		 */
		final EmailAccount account = accountSupplier.get();
		if (account.isImapConfigured()) {
			try {
				logger.info("reading from account '{}'", account);
				receive0(folders, callback);
			} catch (final MailException e) {
				logger.error("error receiving mails", e);
				throw EmailServiceException.receive(e);
			}
		} else {
			logger.warn("imap server not configured");
		}
	}

	private void receive0(final Folders folders, final EmailCallbackHandler callback) {
		final MailApi api = apiSupplier.get();
		final Iterable<FetchedMail> fetchMails = api //
				.selectFolder(folders.incoming()) //
				.fetch();
		for (final FetchedMail fetchedMail : fetchMails) {
			boolean keepMail = false;
			String targetFolder = folders.processed();
			try {
				final GetMail getMail = api.selectMail(fetchedMail).get();
				final Email email = new GetMailAdapter(getMail, accountSupplier.get().getName());
				callback.handle(email);
			} catch (final Exception e) {
				logger.error("error getting mail", e);
				keepMail = !folders.rejectNotMatching();
				targetFolder = folders.rejected();
			}

			try {
				if (!keepMail) {
					api.selectMail(fetchedMail) //
							.selectTargetFolder(targetFolder) //
							.move();
				}
			} catch (final MailException e) {
				logger.error("error moving mail", e);
			}
		}
	}

}
