package org.cmdbuild.servlets.json.schema.taskmanager.event;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.EXECUTABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_TEMPLATE;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;

import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

public class Asynchronous extends JSONBaseWithSpringContext {

	private static class JsonAsynchronousEventTask {

		private final AsynchronousEventTask delegate;

		public JsonAsynchronousEventTask(final AsynchronousEventTask delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(ID)
		public Long getId() {
			return delegate.getId();
		}

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return delegate.getDescription();
		}

		@JsonProperty(ACTIVE)
		public boolean isActive() {
			return delegate.isActive();
		}

		@JsonProperty(CRON_EXPRESSION)
		public String getCronExpression() {
			return delegate.getCronExpression();
		}

		@JsonProperty(EXECUTABLE)
		public boolean executable() {
			return delegate.isExecutable();
		}

		@JsonProperty(CLASS_NAME)
		public String getTargetClassname() {
			return delegate.getTargetClassname();
		}

		@JsonProperty(FILTER)
		public String getFilter() {
			return delegate.getFilter();
		}

		@JsonProperty(NOTIFICATION_ACTIVE)
		public boolean isEmailEnabled() {
			return delegate.isNotificationActive();
		}

		@JsonProperty(NOTIFICATION_EMAIL_ACCOUNT)
		public String getEmailAccount() {
			return delegate.getNotificationAccount();
		}

		@JsonProperty(NOTIFICATION_EMAIL_TEMPLATE)
		public String getEmailTemplate() {
			return delegate.getNotificationTemplate();
		}

	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = CLASS_NAME, required = false) final String classname, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean emailActive, //
			@Parameter(value = NOTIFICATION_EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE, required = false) final String emailTemplate //
	) {
		final AsynchronousEventTask task = AsynchronousEventTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withTargetClass(classname) //
				.withCronExpression(cronExpression) //
				.withFilter(filter) //
				.withNotificationStatus(emailActive) //
				.withNotificationAccount(emailAccount) //
				.withNotificationErrorTemplate(emailTemplate) //
				.build();
		final Long id = taskManagerLogic().create(task);
		return success(id);
	}

	@Admin
	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final AsynchronousEventTask task = AsynchronousEventTask.newInstance() //
				.withId(id) //
				.build();
		final AsynchronousEventTask readed = taskManagerLogic().read(task, AsynchronousEventTask.class);
		return success(new JsonAsynchronousEventTask(readed));
	}

	@Admin
	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(AsynchronousEventTask.class);
		return success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@Admin
	@JSONExported
	public JsonResponse update( //
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = CLASS_NAME, required = false) final String classname, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean emailActive, //
			@Parameter(value = NOTIFICATION_EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE, required = false) final String emailTemplate //
	) {
		final AsynchronousEventTask task = AsynchronousEventTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withTargetClass(classname) //
				.withCronExpression(cronExpression) //
				.withFilter(filter) //
				.withNotificationStatus(emailActive) //
				.withNotificationAccount(emailAccount) //
				.withNotificationErrorTemplate(emailTemplate) //
				.build();
		taskManagerLogic().update(task);
		return success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final AsynchronousEventTask task = AsynchronousEventTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

}
