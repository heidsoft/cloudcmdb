package org.cmdbuild.logic.email;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Multimaps.index;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.data.store.email.EmailConstants.ADDRESSES_SEPARATOR;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.joda.time.DateTime.now;

import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.api.mail.NewMailQueue;
import org.cmdbuild.common.api.mail.NewMailQueue.Callback;
import org.cmdbuild.common.api.mail.QueueableNewMail;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.email.EmailAttachmentsLogic.Attachment;
import org.cmdbuild.logic.email.EmailLogic.Email;
import org.cmdbuild.logic.email.EmailLogic.ForwardingEmail;
import org.cmdbuild.logic.email.EmailLogic.Status;
import org.cmdbuild.logic.email.EmailLogic.Statuses;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.email.AllConfigurationWrapper;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;

public class EmailQueueCommand implements Command, Callback {

	private static final Logger logger = Log.EMAIL;
	private static final Marker MARKER = MarkerFactory.getMarker(EmailQueueCommand.class.getName());

	private static final Predicate<Email> DELAY_ELAPSED = new Predicate<Email>() {

		@Override
		public boolean apply(final Email input) {
			return now().isAfter(input.getDate().plus(input.getDelay()));
		}

	};

	private static final Optional<String> ABSENT = absent();

	private static final Function<Email, Optional<String>> ACCOUNT_NAME_OR_ABSENT = new Function<Email, Optional<String>>() {

		@Override
		public Optional<String> apply(final Email input) {
			return isBlank(input.getAccount()) ? ABSENT : Optional.of(input.getAccount());
		}

	};

	private static class SentEmail extends ForwardingEmail {

		private final Email delegate;

		public SentEmail(final Email delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Email delegate() {
			return delegate;
		}

		@Override
		public Status getStatus() {
			return Statuses.sent();
		}

	};

	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	private final EmailAccountFacade emailAccountFacade;
	private final MailApiFactory mailApiFactory;
	private final EmailLogic emailLogic;
	private final EmailAttachmentsLogic emailAttachmensLogic;
	private final SubjectHandler subjectHandler;

	private Email currentEmail;
	private final Map<Integer, Email> emailByIndex = newHashMap();

	public EmailQueueCommand(final EmailAccountFacade emailAccountFacade, final MailApiFactory mailApiFactory,
			final EmailLogic emailLogic, final EmailAttachmentsLogic emailAttachmensLogic,
			final SubjectHandler subjectHandler) {
		this.emailAccountFacade = emailAccountFacade;
		this.mailApiFactory = mailApiFactory;
		this.emailLogic = emailLogic;
		this.emailAttachmensLogic = emailAttachmensLogic;
		this.subjectHandler = subjectHandler;
	}

	@Override
	public void execute() {
		logger.info(MARKER, "starting e-mail queue management");
		try {
			execute0();
		} catch (final Exception e) {
			logger.error(MARKER, "error executing e-mail queue management", e);
		}
	}

	private void execute0() {
		logger.debug(MARKER, "getting all outgoing e-mails where delay is elapsed");
		final Iterable<Email> elements = from(emailLogic.readAll(outgoing())) //
				.filter(DELAY_ELAPSED) //
				.filter(not(new Predicate<Email>() {

					@Override
					public boolean apply(final Email input) {
						final boolean missingReference = (input.getReference() == null);
						if (missingReference) {
							try {
								logger.debug("deleting e-mail '{}' since its reference is missing ", input);
								emailLogic.deleteWithNoChecks(input);
							} catch (final Exception e) {
								logger.debug("error deleting e-mail", e);
							}
						}
						return missingReference;
					}

				}));
		logger.debug(MARKER, "grouping e-mails by account");
		final Multimap<Optional<String>, Email> emailsByAccount = index(elements, ACCOUNT_NAME_OR_ABSENT);
		for (final Optional<String> accountName : emailsByAccount.keySet()) {
			final Optional<EmailAccount> account;
			if (accountName.isPresent()) {
				logger.debug(MARKER, "account is present, using '{}'", accountName.get());
				account = emailAccountFacade.firstOfOrDefault(asList(accountName.get()));
			} else {
				logger.debug(MARKER, "account is absent, using default one");
				account = emailAccountFacade.defaultAccount();
			}
			if (account.isPresent()) {
				final MailApi api = mailApiFactory.create(AllConfigurationWrapper.of(account.get()));
				logger.debug(MARKER, "creating new mail queue");
				final NewMailQueue queue = api.newMailQueue() //
						.withCallback(this) //
						.withForgiving(true);
				emailByIndex.clear();
				for (final Email email : emailsByAccount.get(accountName)) {
					logger.debug(MARKER, "adding e-mail '{}'", email);
					currentEmail = email;
					try {
						final QueueableNewMail newMail = queue.newMail() //
								.withFrom(defaultIfBlank(email.getFromAddress(), account.get().getAddress())) //
								.withTo(splitAddresses(email.getToAddresses())) //
								.withCc(splitAddresses(email.getCcAddresses())) //
								.withBcc(splitAddresses(email.getBccAddresses())) //
								.withSubject(defaultIfBlank(subjectHandler.compile(email).getSubject(), EMPTY)) //
								.withContent(email.getContent()) //
								.withContentType(CONTENT_TYPE);
						for (final Attachment attachment : emailAttachmensLogic.readAll(email)) {
							final Optional<DataHandler> dataHandler = emailAttachmensLogic.read(email, attachment);
							if (dataHandler.isPresent()) {
								newMail.withAttachment(dataHandler.get(), attachment.getFileName());
							}
						}
						newMail.add();
					} catch (final Exception e) {
						logger.error("error adding e-mail, skipping", e);
					}
				}
				try {
					logger.debug(MARKER, "sending all queued e-mails");
					queue.sendAll();
				} catch (final Exception e) {
					logger.error("error sending queued e-mails", e);
				}
			} else {
				logger.warn(MARKER, "missing account (even default) going to next one");
			}
		}
	}

	private Iterable<String> splitAddresses(final String addresses) {
		return on(ADDRESSES_SEPARATOR) //
				.omitEmptyStrings() //
				.trimResults() //
				.split(defaultString(addresses));
	}

	@Override
	public void added(final int index) {
		emailByIndex.put(index, currentEmail);
	}

	@Override
	public void sent(final int index) {
		final Email email = emailByIndex.get(index);
		logger.debug(MARKER, "e-mail '{}' successfully sent", email);
		emailLogic.updateWithNoChecks(new SentEmail(email));
	}

}
