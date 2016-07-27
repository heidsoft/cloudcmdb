package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.contains;
import static java.util.Arrays.asList;
import static org.cmdbuild.data.store.email.EmailStatus.OUTGOING;
import static org.cmdbuild.data.store.email.Groupables.reference;
import static org.cmdbuild.data.store.email.Groupables.status;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.received;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.sent;
import static org.cmdbuild.services.email.EmailUtils.addLineBreakForHtml;

import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.data.store.email.EmailStatusConverter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class DefaultEmailLogic implements EmailLogic {

	private static enum StatusConverter {
		RECEIVED(EmailStatus.RECEIVED) {

			@Override
			public Status status() {
				return received();
			}

		}, //
		DRAFT(EmailStatus.DRAFT) {

			@Override
			public Status status() {
				return draft();
			}

		}, //
		OUTGOING(EmailStatus.OUTGOING) {

			@Override
			public Status status() {
				return outgoing();
			}

		}, //
		SENT(EmailStatus.SENT) {

			@Override
			public Status status() {
				return sent();
			}

		}, //
		UNDEFINED(null) {

			@Override
			public Status status() {
				return null;
			}

		}, //
		;

		private final EmailStatus value;

		private StatusConverter(final EmailStatus value) {
			this.value = value;
		}

		public abstract Status status();

		public EmailStatus value() {
			return value;
		}

		public static StatusConverter of(final EmailStatus value) {
			for (final StatusConverter element : values()) {
				if (element.value.equals(value)) {
					return element;
				}
			}
			return UNDEFINED;
		}

		public static StatusConverter of(final Status status) {
			return new StatusVisitor() {

				private StatusConverter output;

				public StatusConverter convert() {
					if (status != null) {
						status.accept(this);
					} else {
						output = UNDEFINED;
					}
					return output;
				}

				@Override
				public void visit(final Received status) {
					output = RECEIVED;
				}

				@Override
				public void visit(final Draft status) {
					output = DRAFT;
				}

				@Override
				public void visit(final Sent status) {
					output = SENT;
				}

				@Override
				public void visit(final Outgoing status) {
					output = OUTGOING;
				}

			}.convert();
		}

	}

	private static final Function<Email, org.cmdbuild.data.store.email.Email> LOGIC_TO_STORE = new Function<Email, org.cmdbuild.data.store.email.Email>() {

		@Override
		public org.cmdbuild.data.store.email.Email apply(final Email input) {
			final org.cmdbuild.data.store.email.Email output = new org.cmdbuild.data.store.email.Email(input.getId());
			output.setFromAddress(input.getFromAddress());
			output.setToAddresses(input.getToAddresses());
			output.setCcAddresses(input.getCcAddresses());
			output.setBccAddresses(input.getBccAddresses());
			output.setSubject(input.getSubject());
			output.setContent(input.getContent());
			output.setNotifyWith(input.getNotifyWith());
			output.setDate(input.getDate());
			output.setStatus(StatusConverter.of(input.getStatus()).value());
			output.setReference(input.getReference());
			output.setNoSubjectPrefix(input.isNoSubjectPrefix());
			output.setAccount(input.getAccount());
			output.setTemplate(input.getTemplate());
			output.setKeepSynchronization(input.isKeepSynchronization());
			output.setPromptSynchronization(input.isPromptSynchronization());
			output.setDelay(input.getDelay());
			return output;
		}

	};

	private static final Function<org.cmdbuild.data.store.email.Email, Email> STORE_TO_LOGIC = new Function<org.cmdbuild.data.store.email.Email, Email>() {

		@Override
		public Email apply(final org.cmdbuild.data.store.email.Email input) {
			return EmailImpl.newInstance() //
					.withId(input.getId()) //
					.withFromAddress(input.getFromAddress()) //
					.withToAddresses(input.getToAddresses()) //
					.withCcAddresses(input.getCcAddresses()) //
					.withBccAddresses(input.getBccAddresses()) //
					.withSubject(input.getSubject()) //
					.withContent(addLineBreakForHtml(input.getContent())) //
					.withNotifyWith(input.getNotifyWith()) //
					.withDate(input.getDate()) //
					.withStatus(StatusConverter.of(input.getStatus()).status()) //
					.withReference(input.getReference()) //
					.withNoSubjectPrefix(input.isNoSubjectPrefix()) //
					.withAccount(input.getAccount()) //
					.withTemplate(input.getTemplate()) //
					.withKeepSynchronization(input.isKeepSynchronization()) //
					.withPromptSynchronization(input.isPromptSynchronization()) //
					.withDelay(input.getDelay()) //
					.build();
		}

	};

	private static class TemporaryEmail extends ForwardingEmail {

		private final Email delegate;
		private final boolean temporary;

		public TemporaryEmail(final Email delegate, final boolean temporary) {
			this.delegate = delegate;
			this.temporary = temporary;
		}

		@Override
		protected Email delegate() {
			return delegate;
		}

		@Override
		public boolean isTemporary() {
			return temporary;
		}

	}

	private static final Function<Email, Email> TO_TEMPORARY = new Function<Email, Email>() {

		@Override
		public Email apply(final Email input) {
			return new TemporaryEmail(input, true);
		}

	};

	private final Store<org.cmdbuild.data.store.email.Email> emailStore;
	private final Store<org.cmdbuild.data.store.email.Email> temporaryEmailStore;
	private final EmailStatusConverter emailStatusConverter;
	private final EmailAccountFacade emailAccountFacade;

	public DefaultEmailLogic( //
			final Store<org.cmdbuild.data.store.email.Email> emailStore, //
			final Store<org.cmdbuild.data.store.email.Email> temporaryEmailStore, //
			final EmailStatusConverter emailStatusConverter, //
			final EmailAccountFacade emailAccountFacade //
	) {
		this.emailStore = emailStore;
		this.temporaryEmailStore = temporaryEmailStore;
		this.emailStatusConverter = emailStatusConverter;
		this.emailAccountFacade = emailAccountFacade;
	}

	@Override
	public boolean isEnabled(final String className, final Long cardId) {
		/*
		 * TODO classes should have grants
		 * 
		 * TODO processes should always return false because the behavior is
		 * controlled by process definition
		 */
		return emailAccountFacade.defaultAccount().isPresent();
	}

	@Override
	public Long create(final Email email) {
		final org.cmdbuild.data.store.email.Email storableEmail = LOGIC_TO_STORE.apply(new ForwardingEmail() {

			@Override
			protected Email delegate() {
				return email;
			}

			@Override
			public Long getId() {
				return isTemporary() ? Long.valueOf(generateId()) : super.getId();
			}

			private int generateId() {
				return UUID.randomUUID().hashCode();
			}

			@Override
			public Status getStatus() {
				/*
				 * newly created e-mails are always drafts
				 */
				return draft();
			}

		});
		final Storable stored = storeOf(email).create(storableEmail);
		final Long id = Long.parseLong(stored.getIdentifier());
		return id;
	}

	@Override
	public Iterable<Email> readAll(final Long reference) {
		return from(concat( //
				from(emailStore.readAll(reference(reference))) //
						.transform(STORE_TO_LOGIC), //
				from(temporaryEmailStore.readAll()) //
						.filter(new Predicate<org.cmdbuild.data.store.email.Email>() {

							@Override
							public boolean apply(final org.cmdbuild.data.store.email.Email input) {
								return ObjectUtils.equals(reference, input.getReference());
							}

						}) //
						.transform(STORE_TO_LOGIC) //
						.transform(TO_TEMPORARY) //
		));
	}

	@Override
	public Iterable<Email> readAll(final Status status) {
		return from(concat( //
				from(emailStore.readAll(status(emailStatusConverter.toId(OUTGOING)))) //
						.transform(STORE_TO_LOGIC), //
				from(temporaryEmailStore.readAll()) //
						.filter(new Predicate<org.cmdbuild.data.store.email.Email>() {

							@Override
							public boolean apply(final org.cmdbuild.data.store.email.Email input) {
								return ObjectUtils.equals(status, input.getStatus());
							}

						}) //
						.transform(STORE_TO_LOGIC) //
						.transform(TO_TEMPORARY) //
		));
	}

	@Override
	public Email read(final Email email) {
		final org.cmdbuild.data.store.email.Email read = storeOf(email).read(LOGIC_TO_STORE.apply(email));
		return new TemporaryEmail(STORE_TO_LOGIC.apply(read), email.isTemporary());
	}

	@Override
	public void update(final Email email) {
		final Email read = read(email);
		Validate.isTrue( //
				contains(asList(draft(), outgoing()), read.getStatus()), //
				"cannot update e-mail '%s' due to an invalid status", read);
		if (draft().equals(email.getStatus())) {
			Validate.isTrue( //
					contains(asList(draft(), outgoing()), email.getStatus()), //
					"cannot update e-mail due to an invalid new status", email.getStatus());
		} else if (outgoing().equals(email.getStatus())) {
			Validate.isTrue( //
					contains(asList(outgoing()), email.getStatus()), //
					"cannot update e-mail due to an invalid new status", email.getStatus());
		} else {
			Validate.isTrue( //
					contains(asList(draft(), outgoing()), email.getStatus()), //
					"invalid new status", email.getStatus());
		}

		if (draft().equals(read.getStatus())) {
			updateWithNoChecks(email);
		}
	}

	@Override
	public void updateWithNoChecks(final Email email) {
		final org.cmdbuild.data.store.email.Email storable = LOGIC_TO_STORE.apply(email);
		storeOf(email).update(storable);
	}

	@Override
	public void delete(final Email email) {
		final Email read = read(email);
		Validate.isTrue( //
				contains(asList(draft()), read.getStatus()), //
				"cannot delete e-mail '%s' due to an invalid status", read);
		deleteWithNoChecks(read);
	}

	@Override
	public void deleteWithNoChecks(final Email email) {
		final org.cmdbuild.data.store.email.Email storable = LOGIC_TO_STORE.apply(email);
		storeOf(email).delete(storable);
	}

	private Store<org.cmdbuild.data.store.email.Email> storeOf(final Email email) {
		return email.isTemporary() ? temporaryEmailStore : emailStore;
	}

}
