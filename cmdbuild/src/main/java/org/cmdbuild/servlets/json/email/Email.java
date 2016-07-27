package org.cmdbuild.servlets.json.email;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.received;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.sent;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.BCC;
import static org.cmdbuild.servlets.json.CommunicationConstants.BODY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.CC;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DELAY;
import static org.cmdbuild.servlets.json.CommunicationConstants.FROM;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.KEEP_SYNCHRONIZATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFY_WITH;
import static org.cmdbuild.servlets.json.CommunicationConstants.NO_SUBJECT_PREFIX;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROMPT_SYNCHRONIZATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.REFERENCE;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATUS;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATUS_DRAFT;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATUS_OUTGOING;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATUS_RECEIVED;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATUS_SENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.SUBJECT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPLATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPORARY;
import static org.cmdbuild.servlets.json.CommunicationConstants.TO;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.email.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailLogic.Draft;
import org.cmdbuild.logic.email.EmailLogic.Outgoing;
import org.cmdbuild.logic.email.EmailLogic.Received;
import org.cmdbuild.logic.email.EmailLogic.Sent;
import org.cmdbuild.logic.email.EmailLogic.Status;
import org.cmdbuild.logic.email.EmailLogic.StatusVisitor;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.AbstractJsonResponseSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;

public class Email extends JSONBaseWithSpringContext {

	private static enum StatusConverter {
		RECEIVED(STATUS_RECEIVED) {

			@Override
			public Status status() {
				return received();
			}

		}, //
		DRAFT(STATUS_DRAFT) {

			@Override
			public Status status() {
				return draft();
			}

		}, //
		OUTGOING(STATUS_OUTGOING) {

			@Override
			public Status status() {
				return outgoing();
			}

		}, //
		SENT(STATUS_SENT) {

			@Override
			public Status status() {
				return sent();
			}

		}, //
		;

		private final String value;

		private StatusConverter(final String value) {
			this.value = value;
		}

		public abstract Status status();

		public String string() {
			return value;
		}

		public static StatusConverter of(final String value) {
			for (final StatusConverter element : values()) {
				if (element.value.equalsIgnoreCase(value)) {
					return element;
				}
			}
			throw new IllegalArgumentException(format("value '%s' not found", value));
		}

		public static StatusConverter of(final Status status) {
			return new StatusVisitor() {

				private StatusConverter output;

				public StatusConverter convert() {
					status.accept(this);
					Validate.notNull(output, "invalid status '%s'", status);
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

	private static class JsonEmail extends AbstractJsonResponseSerializer {

		private final EmailLogic.Email delegate;

		private JsonEmail(final EmailLogic.Email delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(ID)
		public Long getId() {
			return delegate.getId();
		}

		@JsonProperty(FROM)
		public String getFromAddress() {
			return delegate.getFromAddress();
		}

		@JsonProperty(TO)
		public String getToAddresses() {
			return delegate.getToAddresses();
		}

		@JsonProperty(CC)
		public String getCcAddresses() {
			return delegate.getCcAddresses();
		}

		@JsonProperty(BCC)
		public String getBccAddresses() {
			return delegate.getBccAddresses();
		}

		@JsonProperty(SUBJECT)
		public String getSubject() {
			return delegate.getSubject();
		}

		@JsonProperty(BODY)
		public String getContent() {
			return delegate.getContent();
		}

		@JsonProperty(DATE)
		public String getDate() {
			return formatDateTime(delegate.getDate());
		}

		@JsonProperty(STATUS)
		public String getStatus() {
			return StatusConverter.of(delegate.getStatus()).string();
		}

		@JsonProperty(REFERENCE)
		public Long getReference() {
			return delegate.getReference();
		}

		@JsonProperty(NOTIFY_WITH)
		public String getNotifyWith() {
			return delegate.getNotifyWith();
		}

		@JsonProperty(NO_SUBJECT_PREFIX)
		public boolean isNoSubjectPrefix() {
			return delegate.isNoSubjectPrefix();
		}

		@JsonProperty(ACCOUNT)
		public String getAccount() {
			return delegate.getAccount();
		}

		@JsonProperty(TEMPORARY)
		public boolean isTemporary() {
			return delegate.isTemporary();
		}

		@JsonProperty(TEMPLATE)
		public String getTemplate() {
			return delegate.getTemplate();
		}

		@JsonProperty(KEEP_SYNCHRONIZATION)
		public boolean isKeepSynchronization() {
			return delegate.isKeepSynchronization();
		}

		@JsonProperty(PROMPT_SYNCHRONIZATION)
		public boolean isPromptSynchronization() {
			return delegate.isPromptSynchronization();
		}

		@JsonProperty(DELAY)
		public long getDelay() {
			return delegate.getDelay();
		}

	}

	private static final Function<EmailLogic.Email, JsonEmail> TO_JSON_EMAIL = new Function<EmailLogic.Email, JsonEmail>() {

		@Override
		public JsonEmail apply(final EmailLogic.Email input) {
			return new JsonEmail(input);
		}

	};

	@JSONExported
	public JsonResponse enabled( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(CARD_ID) final Long cardId //
	) {
		final boolean enabled = emailLogic().isEnabled(className, cardId);
		return JsonResponse.success(enabled);
	}

	@JSONExported
	public JsonResponse create( //
			@Parameter(value = FROM, required = false) final String from, //
			@Parameter(TO) final String to, //
			@Parameter(value = CC, required = false) final String cc, //
			@Parameter(value = BCC, required = false) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = NOTIFY_WITH, required = false) final String notifyWith, //
			@Parameter(value = REFERENCE, required = false) final Long reference, //
			@Parameter(value = NO_SUBJECT_PREFIX, required = false) final boolean noSubjectPrefix, //
			@Parameter(value = ACCOUNT, required = false) final String account, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = TEMPLATE, required = false) final String template, //
			@Parameter(value = KEEP_SYNCHRONIZATION, required = false) final boolean keepSynchronization, //
			@Parameter(value = PROMPT_SYNCHRONIZATION, required = false) final boolean promptSynchronization, //
			@Parameter(value = DELAY, required = false) final long delay //
	) {
		final Long id = emailLogic().create(EmailImpl.newInstance() //
				.withFromAddress(from) //
				.withToAddresses(to) //
				.withCcAddresses(cc) //
				.withBccAddresses(bcc) //
				.withSubject(subject) //
				.withContent(body) //
				.withNotifyWith(notifyWith) //
				.withStatus(draft()) //
				.withReference(reference) //
				.withNoSubjectPrefix(noSubjectPrefix) //
				.withAccount(account) //
				.withTemporary(temporary) //
				.withTemplate(template) //
				.withKeepSynchronization(keepSynchronization) //
				.withPromptSynchronization(promptSynchronization) //
				.withDelay(delay) //
				.build());
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse readAll( //
			@Parameter(REFERENCE) final Long reference //
	) {
		final Iterable<EmailLogic.Email> emails = emailLogic().readAll(reference);
		return JsonResponse.success(from(emails) //
				.transform(TO_JSON_EMAIL) //
				.toList());
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(ID) final Long id, //
			@Parameter(TEMPORARY) final boolean temporary //
	) {
		final EmailLogic.Email read = emailLogic().read(EmailImpl.newInstance() //
				.withId(id) //
				.withTemporary(temporary) //
				.build());
		return JsonResponse.success(TO_JSON_EMAIL.apply(read));
	}

	@JSONExported
	public JsonResponse update( //
			@Parameter(ID) final Long id, //
			@Parameter(value = FROM, required = false) final String from, //
			@Parameter(TO) final String to, //
			@Parameter(value = CC, required = false) final String cc, //
			@Parameter(value = BCC, required = false) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body, //
			@Parameter(value = STATUS) final String status, //
			@Parameter(value = NOTIFY_WITH, required = false) final String notifyWith, //
			@Parameter(value = REFERENCE, required = false) final Long reference, //
			@Parameter(value = NO_SUBJECT_PREFIX, required = false) final boolean noSubjectPrefix, //
			@Parameter(value = ACCOUNT, required = false) final String account, //
			@Parameter(value = TEMPORARY, required = false) final boolean temporary, //
			@Parameter(value = TEMPLATE, required = false) final String template, //
			@Parameter(value = KEEP_SYNCHRONIZATION, required = false) final boolean keepSynchronization, //
			@Parameter(value = PROMPT_SYNCHRONIZATION, required = false) final boolean promptSynchronization, //
			@Parameter(value = DELAY, required = false) final long delay //
	) {
		emailLogic().update(EmailImpl.newInstance() //
				.withId(id) //
				.withFromAddress(from) //
				.withToAddresses(to) //
				.withCcAddresses(cc) //
				.withBccAddresses(bcc) //
				.withSubject(subject) //
				.withContent(body) //
				.withNotifyWith(notifyWith) //
				.withStatus(StatusConverter.of(status).status()) //
				.withReference(reference) //
				.withNoSubjectPrefix(noSubjectPrefix) //
				.withAccount(account) //
				.withTemporary(temporary) //
				.withTemplate(template) //
				.withKeepSynchronization(keepSynchronization) //
				.withPromptSynchronization(promptSynchronization) //
				.withDelay(delay) //
				.build());
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse delete( //
			@Parameter(ID) final Long id, //
			@Parameter(TEMPORARY) final boolean temporary //
	) {
		emailLogic().delete(EmailImpl.newInstance() //
				.withId(id) //
				.withTemporary(temporary) //
				.build());
		return JsonResponse.success(id);
	}

}
