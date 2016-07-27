package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.received;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.sent;
import static org.cmdbuild.service.rest.v2.model.Models.newEmail;
import static org.cmdbuild.service.rest.v2.model.Models.newLongId;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.util.NoSuchElementException;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.logic.email.EmailImpl;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v2.ProcessInstanceEmails;
import org.cmdbuild.service.rest.v2.cxf.serialization.DefaultConverter;
import org.cmdbuild.service.rest.v2.model.Email;
import org.cmdbuild.service.rest.v2.model.LongId;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CxfProcessInstanceEmails implements ProcessInstanceEmails {

	private static class LogicToLong implements Function<EmailLogic.Email, LongId> {

		@Override
		public LongId apply(final org.cmdbuild.logic.email.EmailLogic.Email input) {
			return newLongId() //
					.withId(input.getId()) //
					.build();
		}

	}

	private static final LogicToLong LOGIC_TO_LONG = new LogicToLong();

	private static class LogicToRest implements Function<EmailLogic.Email, Email> {

		private final DateAttributeType DATE_ATTRIBUTE_TYPE = new DateAttributeType();

		@Override
		public Email apply(final org.cmdbuild.logic.email.EmailLogic.Email input) {
			return newEmail() //
					.withId(input.getId()) //
					.withFrom(input.getFromAddress()) //
					.withTo(input.getToAddresses()) //
					.withCc(input.getCcAddresses()) //
					.withBcc(input.getBccAddresses()) //
					.withSubject(input.getSubject()) //
					.withBody(input.getContent()) //
					.withDate(dateAsString(input.getDate())) //
					.withStatus(statuses.inverse().get(input.getStatus())) //
					.withNotifyWith(input.getNotifyWith()) //
					.withNoSubjectPrefix(input.isNoSubjectPrefix()) //
					.withAccount(input.getAccount()) //
					.withTemplate(input.getTemplate()) //
					.withKeepSynchronization(input.isKeepSynchronization()) //
					.withPromptSynchronization(input.isPromptSynchronization()) //
					.withDelay(input.getDelay()) //
					.build();
		}

		private String dateAsString(final DateTime input) {
			return (input == null) ? null : DefaultConverter.newInstance() //
					.build() //
					.toClient() //
					.convert(DATE_ATTRIBUTE_TYPE, input.toDate()) //
					.toString();

		}

	};

	private static final LogicToRest LOGIC_TO_REST = new LogicToRest();

	private static class RestToLogic implements Function<Email, EmailLogic.Email> {

		private final Predicate<Long> predicate;
		private final Long activityId;

		public RestToLogic(final Predicate<Long> predicate, final Long activityId) {
			this.predicate = predicate;
			this.activityId = activityId;
		}

		@Override
		public EmailLogic.Email apply(final Email input) {
			return EmailImpl.newInstance() //
					.withId(input.getId()) //
					.withFromAddress(input.getFrom()) //
					.withToAddresses(input.getTo()) //
					.withCcAddresses(input.getCc()) //
					.withBccAddresses(input.getBcc()) //
					.withSubject(input.getSubject()) //
					.withContent(input.getBody()) //
					.withStatus(statuses.get(input.getStatus())) //
					.withNotifyWith(input.getNotifyWith()) //
					.withNoSubjectPrefix(input.isNoSubjectPrefix()) //
					.withAccount(input.getAccount()) //
					.withTemporary(predicate.apply(activityId)) //
					.withTemplate(input.getTemplate()) //
					.withKeepSynchronization(input.isKeepSynchronization()) //
					.withPromptSynchronization(input.isPromptSynchronization()) //
					.withDelay(input.getDelay()) //
					.build();
		}

	};

	private static final BiMap<String, EmailLogic.Status> statuses = HashBiMap.create();

	static {
		statuses.put("received", received());
		statuses.put("draft", draft());
		statuses.put("outgoing", outgoing());
		statuses.put("sent", sent());
	}

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;
	private final EmailLogic emailLogic;
	private final Predicate<Long> temporaryPredicate;

	public CxfProcessInstanceEmails(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic,
			final EmailLogic emailLogic, final IdGenerator idGenerator) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
		this.emailLogic = emailLogic;
		this.temporaryPredicate = new Predicate<Long>() {

			@Override
			public boolean apply(final Long input) {
				return idGenerator.isGenerated(input);
			};

		};
	}

	@Override
	public ResponseMultiple<String> statuses() {
		return newResponseMultiple(String.class) //
				.withElements(statuses.keySet()) //
				.withMetadata(newMetadata() //
						.withTotal(statuses.size()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Long> create(final String processId, final Long processInstanceId, final Email email) {
		checkPreconditions(processId, processInstanceId > 0 ? processInstanceId : null);
		final Long id = emailLogic.create(new EmailLogic.ForwardingEmail() {

			@Override
			protected org.cmdbuild.logic.email.EmailLogic.Email delegate() {
				return new RestToLogic(temporaryPredicate, processInstanceId).apply(email);
			}

			@Override
			public Long getReference() {
				return processInstanceId;
			}

		});
		return newResponseSingle(Long.class) //
				.withElement(id) //
				.build();
	}

	@Override
	public ResponseMultiple<LongId> readAll(final String processId, final Long processInstanceId, final Integer limit,
			final Integer offset) {
		checkPreconditions(processId, null);
		final Iterable<EmailLogic.Email> elements = emailLogic.readAll(processInstanceId);
		return newResponseMultiple(LongId.class) //
				.withElements(from(elements) //
						.skip((offset == null) ? 0 : offset) //
						.limit((limit == null) ? MAX_VALUE : limit) //
						.transform(LOGIC_TO_LONG) //
				) //
				.withMetadata(newMetadata() //
						.withTotal(size(elements)) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Email> read(final String processId, final Long processInstanceId, final Long emailId) {
		checkPreconditions(processId, null);
		final EmailLogic.Email element = emailLogic.read(EmailImpl.newInstance() //
				.withId(emailId) //
				.build());
		return newResponseSingle(Email.class) //
				.withElement(LOGIC_TO_REST.apply(element)) //
				.build();
	}

	@Override
	public void update(final String processId, final Long processInstanceId, final Long emailId, final Email email) {
		checkPreconditions(processId, processInstanceId > 0 ? processInstanceId : null);
		emailLogic.update(new EmailLogic.ForwardingEmail() {

			@Override
			protected org.cmdbuild.logic.email.EmailLogic.Email delegate() {
				return new RestToLogic(temporaryPredicate, processInstanceId).apply(email);
			}

			@Override
			public Long getId() {
				return emailId;
			}

			@Override
			public Long getReference() {
				return processInstanceId;
			}

		});
	}

	@Override
	public void delete(final String processId, final Long processInstanceId, final Long emailId) {
		checkPreconditions(processId, null);
		emailLogic.delete(EmailImpl.newInstance() //
				.withId(emailId) //
				.build());
	}

	private void checkPreconditions(final String classId, final Long cardId) {
		final CMClass targetClass = workflowLogic.findProcessClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		if (cardId != null) {
			try {
				workflowLogic.getProcessInstance(classId, cardId);
			} catch (final NoSuchElementException e) {
				errorHandler.cardNotFound(cardId);
			}
		}
	}

}
