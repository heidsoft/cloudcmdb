package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.cmdbuild.logic.dms.Utils.valueForCategory;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTACHMENTS_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTACHMENTS_CATEGORY;
import static org.cmdbuild.servlets.json.CommunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.EMAIL_ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.EXECUTABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER_FROM_ADDRESS;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER_FUNCTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER_SUBJECT;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.INCOMING_FOLDER;
import static org.cmdbuild.servlets.json.CommunicationConstants.MAPPER_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.MAPPER_KEY_END;
import static org.cmdbuild.servlets.json.CommunicationConstants.MAPPER_KEY_INIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.MAPPER_VALUE_END;
import static org.cmdbuild.servlets.json.CommunicationConstants.MAPPER_VALUE_INIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_TEMPLATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROCESSED_FOLDER;
import static org.cmdbuild.servlets.json.CommunicationConstants.REJECTED_FOLDER;
import static org.cmdbuild.servlets.json.CommunicationConstants.REJECT_NOT_MATCHING;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ADVANCEABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ATTACHMENTS_CATEGORY;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_SAVE_ATTACHMENTS;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Map;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.email.mapper.KeyValueMapperEngine;
import org.cmdbuild.logic.taskmanager.task.email.mapper.MapperEngine;
import org.cmdbuild.logic.taskmanager.task.email.mapper.NullMapperEngine;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ReadEmail extends JSONBaseWithSpringContext {

	private static class JsonReadEmailTask {

		private final Map<String, Lookup> categoryLookupsByName;
		private final ReadEmailTask delegate;
		private final KeyValueMapperEngine engine;

		public JsonReadEmailTask(final Map<String, Lookup> categoryLookupsByName, final ReadEmailTask delegate) {
			this.categoryLookupsByName = categoryLookupsByName;
			this.delegate = delegate;
			final MapperEngine current = delegate.getMapperEngine();
			this.engine = (current instanceof KeyValueMapperEngine) ? KeyValueMapperEngine.class.cast(current) : null;
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

		@JsonProperty(EMAIL_ACCOUNT)
		public String getEmailAccount() {
			return delegate.getEmailAccount();
		}

		@JsonProperty(INCOMING_FOLDER)
		public String getInputFolder() {
			return delegate.getIncomingFolder();
		}

		@JsonProperty(PROCESSED_FOLDER)
		public String getProcessedFolder() {
			return delegate.getProcessedFolder();
		}

		@JsonProperty(REJECTED_FOLDER)
		public String getRejectedFolder() {
			return delegate.getRejectedFolder();
		}

		@JsonProperty(REJECT_NOT_MATCHING)
		public boolean isRejectNotMatching() {
			return delegate.isRejectNotMatching();
		}

		@JsonProperty(FILTER_TYPE)
		public String getFilterType() {
			return delegate.getFilterType();
		}

		@JsonProperty(FILTER_FROM_ADDRESS)
		// TODO send array as string?
		public Iterable<String> getRegexFromFilter() {
			return delegate.getRegexFromFilter();
		}

		@JsonProperty(FILTER_SUBJECT)
		// TODO send array as string?
		public Iterable<String> getRegexSubjectFilter() {
			return delegate.getRegexSubjectFilter();
		}

		@JsonProperty(FILTER_FUNCTION)
		public String getFilterFunction() {
			return delegate.getFilterFunction();
		}

		@JsonProperty(NOTIFICATION_ACTIVE)
		public boolean isNotificationActive() {
			return delegate.isNotificationActive();
		}

		@JsonProperty(NOTIFICATION_EMAIL_TEMPLATE)
		public String getNotificationTemplate() {
			return delegate.getNotificationTemplate();
		}

		@JsonProperty(ATTACHMENTS_ACTIVE)
		public boolean isAttachmentsActive() {
			return delegate.isAttachmentsActive();
		}

		@JsonProperty(ATTACHMENTS_CATEGORY)
		public Long getAttachmentsCategory() {
			return lookupIdOf(delegate.getAttachmentsCategory());
		}

		@JsonProperty(WORKFLOW_ACTIVE)
		public boolean isWorkflowActive() {
			return delegate.isWorkflowActive();
		}

		@JsonProperty(WORKFLOW_CLASS_NAME)
		public String getWorkflowClassName() {
			return delegate.getWorkflowClassName();
		}

		@JsonProperty(WORKFLOW_ATTRIBUTES)
		// TODO send object as string?
		public Map<String, String> getWorkflowAttributes() {
			return delegate.getWorkflowAttributes();
		}

		@JsonProperty(WORKFLOW_ADVANCEABLE)
		public boolean isWorkflowAdvanceable() {
			return delegate.isWorkflowAdvanceable();
		}

		@JsonProperty(WORKFLOW_SAVE_ATTACHMENTS)
		public boolean isWorkflowAttachments() {
			return delegate.isWorkflowAttachments();
		}

		@JsonProperty(WORKFLOW_ATTACHMENTS_CATEGORY)
		public Long getWorkflowAttachmentsCategory() {
			return lookupIdOf(delegate.getWorkflowAttachmentsCategory());
		}

		@JsonProperty(MAPPER_ACTIVE)
		public boolean isMapperActive() {
			return (engine != null);
		}

		@JsonProperty(MAPPER_KEY_INIT)
		public String getKeyInit() {
			return (engine == null) ? null : engine.getKeyInit();
		}

		@JsonProperty(MAPPER_KEY_END)
		public String getKeyEnd() {
			return (engine == null) ? null : engine.getKeyEnd();
		}

		@JsonProperty(MAPPER_VALUE_INIT)
		public String getValueInit() {
			return (engine == null) ? null : engine.getValueInit();
		}

		@JsonProperty(MAPPER_VALUE_END)
		public String getValueEnd() {
			return (engine == null) ? null : engine.getValueEnd();
		}

		private Long lookupIdOf(final String category) {
			final Lookup lookup = categoryLookupsByName.get(category);
			return (lookup == null) ? null : lookup.getId();
		}

	}

	@Admin
	@JSONExported
	public JsonResponse create(
			//
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = INCOMING_FOLDER, required = false) final String incomingFolder, //
			@Parameter(value = PROCESSED_FOLDER, required = false) final String processedFolder, //
			@Parameter(value = REJECTED_FOLDER, required = false) final String rejectedFolder, //
			@Parameter(value = REJECT_NOT_MATCHING, required = false) final boolean rejectNotMatching, //
			@Parameter(value = FILTER_TYPE, required = false) final String filterType, //
			@Parameter(value = FILTER_FROM_ADDRESS, required = false) final JSONArray filterFromAddress, //
			@Parameter(value = FILTER_SUBJECT, required = false) final JSONArray filterSubject, //
			@Parameter(value = FILTER_FUNCTION, required = false) final String filterFunction, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean notificationActive, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = ATTACHMENTS_ACTIVE, required = false) final Boolean attachmentsActive, //
			@Parameter(value = ATTACHMENTS_CATEGORY, required = false) final Long attachmentsCategoryId, //
			@Parameter(value = WORKFLOW_ACTIVE, required = false) final Boolean workflowActive, //
			@Parameter(value = WORKFLOW_CLASS_NAME, required = false) final String workflowClassName, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject workflowAttributes, //
			@Parameter(value = WORKFLOW_SAVE_ATTACHMENTS, required = false) final Boolean workflowSaveAttachments, //
			@Parameter(value = WORKFLOW_ATTACHMENTS_CATEGORY, required = false) final String workflowAttachmentsCategoryId, //
			@Parameter(value = MAPPER_ACTIVE, required = false) final Boolean mapperActive, //
			@Parameter(value = MAPPER_KEY_INIT, required = false) final String keyInit, //
			@Parameter(value = MAPPER_KEY_END, required = false) final String keyEnd, //
			@Parameter(value = MAPPER_VALUE_INIT, required = false) final String valueInit, //
			@Parameter(value = MAPPER_VALUE_END, required = false) final String valueEnd //
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				//
				.withEmailAccount(emailAccount) //
				.withIncomingFolder(incomingFolder) //
				.withProcessedFolder(processedFolder) //
				.withRejectedFolder(rejectedFolder) //
				.withRejectNotMatching(rejectNotMatching) //
				//
				// filters
				.withFilterType(filterType) //
				.withRegexFromFilter(toIterable(filterFromAddress)) //
				.withRegexSubjectFilter(toIterable(filterSubject)) //
				.withFilterFunction(filterFunction) //
				//
				// send notification
				.withNotificationStatus(defaultIfNull(notificationActive, false)) //
				.withNotificationTemplate(defaultIfNull(emailTemplate, null)) //
				//
				// store attachments
				.withAttachmentsActive(attachmentsActive) //
				.withAttachmentsCategory(lookupValueOf(attachmentsCategoryId)) //
				//
				// workflow (start process and, maybe, store attachments)
				.withWorkflowActive(workflowActive) //
				.withWorkflowClassName(workflowClassName) //
				.withWorkflowAttributes(toMap(workflowAttributes)) //
				.withWorkflowAdvanceableStatus(true) //
				.withWorkflowAttachmentsStatus(workflowSaveAttachments) //
				.withWorkflowAttachmentsCategory(workflowAttachmentsCategoryId) //
				//
				// mapping
				.withMapperEngine(mapperEngine(mapperActive, keyInit, keyEnd, valueInit, valueEnd) //
		)
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
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.build();
		final ReadEmailTask readed = taskManagerLogic().read(task, ReadEmailTask.class);
		return success(toJson(readed));
	}

	@Admin
	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(ReadEmailTask.class);
		return success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@Admin
	@JSONExported
	public JsonResponse update(
			//
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = INCOMING_FOLDER, required = false) final String incomingFolder, //
			@Parameter(value = PROCESSED_FOLDER, required = false) final String processedFolder, //
			@Parameter(value = REJECTED_FOLDER, required = false) final String rejectedFolder, //
			@Parameter(value = REJECT_NOT_MATCHING, required = false) final boolean rejectNotMatching, //
			@Parameter(value = FILTER_TYPE, required = false) final String filterType, //
			@Parameter(value = FILTER_FROM_ADDRESS, required = false) final JSONArray filterFromAddress, //
			@Parameter(value = FILTER_SUBJECT, required = false) final JSONArray filterSubject, //
			@Parameter(value = FILTER_FUNCTION, required = false) final String filterFunction, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean notificationActive, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = ATTACHMENTS_ACTIVE, required = false) final Boolean attachmentsActive, //
			@Parameter(value = ATTACHMENTS_CATEGORY, required = false) final Long attachmentsCategoryId, //
			@Parameter(value = WORKFLOW_ACTIVE, required = false) final Boolean workflowActive, //
			@Parameter(value = WORKFLOW_CLASS_NAME, required = false) final String workflowClassName, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject workflowAttributes, //
			@Parameter(value = WORKFLOW_SAVE_ATTACHMENTS, required = false) final Boolean workflowSaveAttachments, //
			@Parameter(value = WORKFLOW_ATTACHMENTS_CATEGORY, required = false) final String workflowAttachmentsCategoryId, //
			@Parameter(value = MAPPER_ACTIVE, required = false) final Boolean mapperActive, //
			@Parameter(value = MAPPER_KEY_INIT, required = false) final String keyInit, //
			@Parameter(value = MAPPER_KEY_END, required = false) final String keyEnd, //
			@Parameter(value = MAPPER_VALUE_INIT, required = false) final String valueInit, //
			@Parameter(value = MAPPER_VALUE_END, required = false) final String valueEnd //
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				//
				.withEmailAccount(emailAccount) //
				.withIncomingFolder(incomingFolder) //
				.withProcessedFolder(processedFolder) //
				.withRejectedFolder(rejectedFolder) //
				.withRejectNotMatching(rejectNotMatching) //
				//
				// filters
				.withFilterType(filterType) //
				.withRegexFromFilter(toIterable(filterFromAddress)) //
				.withRegexSubjectFilter(toIterable(filterSubject)) //
				.withFilterFunction(filterFunction) //
				//
				// send notification
				.withNotificationStatus(defaultIfNull(notificationActive, false)) //
				.withNotificationTemplate(defaultIfNull(emailTemplate, null)) //
				//
				// store attachments
				.withAttachmentsActive(attachmentsActive) //
				.withAttachmentsCategory(lookupValueOf(attachmentsCategoryId)) //
				//
				// workflow (start process and, maybe, store attachments)
				.withWorkflowActive(workflowActive) //
				.withWorkflowClassName(workflowClassName) //
				.withWorkflowAttributes(toMap(workflowAttributes)) //
				.withWorkflowAdvanceableStatus(true) //
				.withWorkflowAttachmentsStatus(workflowSaveAttachments) //
				.withWorkflowAttachmentsCategory(workflowAttachmentsCategoryId) //
				//
				// mapping
				.withMapperEngine(mapperEngine(mapperActive, keyInit, keyEnd, valueInit, valueEnd))
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
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

	private String lookupValueOf(final Long id) {
		final Lookup lookup;
		if (id == 0) {
			lookup = null;
		} else {
			lookup = lookupLogic().getLookup(id);
		}
		return (lookup == null) ? null : valueForCategory(lookup);
	}

	private MapperEngine mapperEngine(final Boolean active, final String keyInit, final String keyEnd,
			final String valueInit, final String valueEnd) {
		final MapperEngine engine;
		if (!defaultIfNull(active, false)) {
			engine = NullMapperEngine.getInstance();
		} else {
			engine = KeyValueMapperEngine.newInstance() //
					.withKey(keyInit, keyEnd) //
					.withValue(valueInit, valueEnd) //
					.build();
		}
		return engine;
	}

	private JsonReadEmailTask toJson(final ReadEmailTask readed) {
		final String categoryLookupType = dmsConfiguration().getCmdbuildCategory();
		final Iterable<Lookup> categoryLookups = lookupStore().readAll(a(LookupType.newInstance() //
				.withName(categoryLookupType)));
		final Map<String, Lookup> categoryLookupsByName = Maps.uniqueIndex(categoryLookups,
				new Function<Lookup, String>() {

					@Override
					public String apply(final Lookup input) {
						return valueForCategory(input);
					}

				});
		return new JsonReadEmailTask(categoryLookupsByName, readed);
	}
}
