package org.cmdbuild.servlets.json.schema.taskmanager.event;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.EXECUTABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_TEMPLATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE_AFTER_CREATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE_AFTER_UPDATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE_BEFORE_DELETE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE_BEFORE_UPDATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ADVANCEABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_CLASS_NAME;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask.Phase;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class Synchronous extends JSONBaseWithSpringContext {

	private static enum JsonPhase {
		AFTER_CREATE(PHASE_AFTER_CREATE, Phase.AFTER_CREATE), //
		BEFORE_UPDATE(PHASE_BEFORE_UPDATE, Phase.BEFORE_UPDATE), //
		AFTER_UPDATE(PHASE_AFTER_UPDATE, Phase.AFTER_UPDATE), //
		BEFORE_DELETE(PHASE_BEFORE_DELETE, Phase.BEFORE_DELETE), //
		UNKNOWN(EMPTY, null), //
		;

		public static JsonPhase of(final String jsonString) {
			for (final JsonPhase element : values()) {
				if (element.jsonString.equals(jsonString)) {
					return element;
				}
			}
			return UNKNOWN;
		}

		public static JsonPhase of(final Phase phase) {
			for (final JsonPhase element : values()) {
				if (ObjectUtils.equals(element.phase, phase)) {
					return element;
				}
			}
			return UNKNOWN;
		}

		private final String jsonString;
		private final Phase phase;

		private JsonPhase(final String jsonString, final Phase phase) {
			this.jsonString = jsonString;
			this.phase = phase;
		}

		public String toJsonString() {
			return jsonString;
		}

		public Phase toPhase() {
			return phase;
		}

	}

	private static class JsonSynchronousEventTask {

		private final SynchronousEventTask delegate;

		public JsonSynchronousEventTask(final SynchronousEventTask delegate) {
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

		@JsonProperty(EXECUTABLE)
		public boolean executable() {
			return delegate.isExecutable();
		}

		@JsonProperty(PHASE)
		public String getPhase() {
			return JsonPhase.of(delegate.getPhase()).toJsonString();
		}

		@JsonProperty(GROUPS)
		public Iterable<String> getGroups() {
			return delegate.getGroups();
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
			return delegate.isEmailEnabled();
		}

		@JsonProperty(NOTIFICATION_EMAIL_ACCOUNT)
		public String getEmailAccount() {
			return delegate.getEmailAccount();
		}

		@JsonProperty(NOTIFICATION_EMAIL_TEMPLATE)
		public String getEmailTemplate() {
			return delegate.getEmailTemplate();
		}

		@JsonProperty(WORKFLOW_ACTIVE)
		public boolean isWorkflowEnabled() {
			return delegate.isWorkflowEnabled();
		}

		@JsonProperty(WORKFLOW_CLASS_NAME)
		public String getWorkflowClassName() {
			return delegate.getWorkflowClassName();
		}

		@JsonProperty(WORKFLOW_ATTRIBUTES)
		public Map<String, String> getWorkflowAttributes() {
			return delegate.getWorkflowAttributes();
		}

		@JsonProperty(WORKFLOW_ADVANCEABLE)
		public boolean isWorkflowAdvanceable() {
			return delegate.isWorkflowAdvanceable();
		}

	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(value = PHASE, required = false) final String phase, //
			@Parameter(value = GROUPS, required = false) final JSONArray groups, //
			@Parameter(value = CLASS_NAME, required = false) final String classname, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean emailActive, //
			@Parameter(value = NOTIFICATION_EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = WORKFLOW_ACTIVE, required = false) final Boolean workflowActive, //
			@Parameter(value = WORKFLOW_CLASS_NAME, required = false) final String workflowClassName, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject workflowAttributes //
	) {
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				//
				// phase
				.withPhase(JsonPhase.of(phase).toPhase()) //
				//
				// filtering
				.withGroups(toIterable(groups)) //
				.withTargetClass(classname) //
				.withFilter(filter) //
				//
				// send notification
				.withEmailEnabled(emailActive) //
				.withEmailAccount(emailAccount) //
				.withEmailTemplate(emailTemplate) //
				//
				// start process
				.withWorkflowEnabled(workflowActive) //
				.withWorkflowClassName(workflowClassName) //
				.withWorkflowAttributes(toMap(workflowAttributes)) //
				.withWorkflowAdvanceable(true) //
				//
				.build();
		final Long id = taskManagerLogic().create(task);
		return JsonResponse.success(id);
	}

	@Admin
	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(id) //
				.build();
		final SynchronousEventTask readed = taskManagerLogic().read(task, SynchronousEventTask.class);
		return JsonResponse.success(new JsonSynchronousEventTask(readed));
	}

	@Admin
	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(SynchronousEventTask.class);
		return JsonResponse.success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@Admin
	@JSONExported
	public JsonResponse update( //
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(value = PHASE, required = false) final String phase, //
			@Parameter(value = GROUPS, required = false) final JSONArray groups, //
			@Parameter(value = CLASS_NAME, required = false) final String classname, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean emailActive, //
			@Parameter(value = NOTIFICATION_EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = WORKFLOW_ACTIVE, required = false) final Boolean workflowActive, //
			@Parameter(value = WORKFLOW_CLASS_NAME, required = false) final String workflowClassName, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject workflowAttributes //
	) {
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				//
				// phase
				.withPhase(JsonPhase.of(phase).toPhase()) //
				//
				// filtering
				.withGroups(toIterable(groups)) //
				.withTargetClass(classname) //
				.withFilter(filter) //
				//
				// send notification
				.withEmailEnabled(emailActive) //
				.withEmailAccount(emailAccount) //
				.withEmailTemplate(emailTemplate) //
				//
				// start process
				.withWorkflowEnabled(workflowActive) //
				.withWorkflowClassName(workflowClassName) //
				.withWorkflowAttributes(toMap(workflowAttributes)) //
				.withWorkflowAdvanceable(true) //
				//
				.build();
		taskManagerLogic().update(task);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final SynchronousEventTask task = SynchronousEventTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

}
