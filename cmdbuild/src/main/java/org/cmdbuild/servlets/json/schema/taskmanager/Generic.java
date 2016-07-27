package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CONTEXT;
import static org.cmdbuild.servlets.json.CommunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.EMAIL_ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.EMAIL_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.EMAIL_TEMPLATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.EXECUTABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.REPORTNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.REPORT_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.REPORT_EXTENSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.REPORT_PARAMETERS;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONObject;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Generic extends JSONBaseWithSpringContext {

	private static final Marker marker = MarkerFactory.getMarker(Generic.class.getName());

	private static class JsonGenericTask {

		private final GenericTask delegate;

		public JsonGenericTask(final GenericTask delegate) {
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
		public boolean isExecutable() {
			return delegate.isExecutable();
		}

		@JsonProperty(CONTEXT)
		public Map<String, Map<String, String>> getContext() {
			return delegate.getContext();
		}

		@JsonProperty(EMAIL_ACTIVE)
		public boolean isEmailActive() {
			return delegate.isEmailActive();
		}

		@JsonProperty(EMAIL_TEMPLATE)
		public String getEmailTemplate() {
			return delegate.getEmailTemplate();
		}

		@JsonProperty(EMAIL_ACCOUNT)
		public String getEmailAcount() {
			return delegate.getEmailAccount();
		}

		@JsonProperty(REPORT_ACTIVE)
		public boolean isReportActive() {
			return delegate.isReportActive();
		}

		@JsonProperty(REPORTNAME)
		public String getReportName() {
			return delegate.getReportName();
		}

		@JsonProperty(REPORT_EXTENSION)
		public String getReportExtension() {
			return delegate.getReportExtension();
		}

		@JsonProperty(REPORT_PARAMETERS)
		public Map<String, String> getReportParameters() {
			return delegate.getReportParameters();
		}

	}

	private static Map<String, Map<String, String>> context(final JSONObject json) {
		try {
			final Map<String, Map<String, String>> map = new HashMap<>();
			if (json != null && json.length() > 0) {
				for (final String key : JSONObject.getNames(json)) {
					map.put(key, toMap(json.getJSONObject(key)));
				}
			}
			return map;
		} catch (final Exception e) {
			logger.warn(marker, "error parsing json data");
			throw new RuntimeException(e);
		}
	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = CONTEXT, required = false) final JSONObject context, //
			@Parameter(value = EMAIL_ACTIVE, required = false) final Boolean emailActive, //
			@Parameter(value = EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = REPORT_ACTIVE, required = false) final Boolean reportActive, //
			@Parameter(value = REPORTNAME, required = false) final String reportName, //
			@Parameter(value = REPORT_EXTENSION, required = false) final String reportExtension, //
			@Parameter(value = REPORT_PARAMETERS, required = false) final JSONObject reportParameters //
	) {
		final GenericTask task = GenericTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				//
				.withContext(context(context)) //
				//
				.withEmailActive(emailActive) //
				.withEmailTemplate(emailTemplate) //
				.withEmailAccount(emailAccount) //
				.withReportActive(reportActive) //
				.withReportName(reportName) //
				.withReportExtension(reportExtension) //
				.withReportParameters(toMap(reportParameters)) //
				//
				.build();
		final Long id = taskManagerLogic().create(task);
		return success(id);
	}

	@Admin
	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final GenericTask task = GenericTask.newInstance() //
				.withId(id) //
				.build();
		final GenericTask readed = taskManagerLogic().read(task, GenericTask.class);
		return success(new JsonGenericTask(readed));
	}

	@Admin
	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(GenericTask.class);
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
			@Parameter(value = CONTEXT, required = false) final JSONObject context, //
			@Parameter(value = EMAIL_ACTIVE, required = false) final Boolean emailActive, //
			@Parameter(value = EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = REPORT_ACTIVE, required = false) final Boolean reportActive, //
			@Parameter(value = REPORTNAME, required = false) final String reportName, //
			@Parameter(value = REPORT_EXTENSION, required = false) final String reportExtension, //
			@Parameter(value = REPORT_PARAMETERS, required = false) final JSONObject reportParameters //
	) {
		final GenericTask task = GenericTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				//
				.withContext(context(context)) //
				//
				.withEmailActive(emailActive) //
				.withEmailTemplate(emailTemplate) //
				.withEmailAccount(emailAccount) //
				.withReportActive(reportActive) //
				.withReportName(reportName) //
				.withReportExtension(reportExtension) //
				.withReportParameters(toMap(reportParameters)) //
				//
				.build();
		taskManagerLogic().update(task);
		return success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final GenericTask task = GenericTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

}
