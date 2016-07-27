package org.cmdbuild.logic.taskmanager.task.email;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.TaskVisitor;
import org.cmdbuild.logic.taskmanager.task.email.mapper.MapperEngine;
import org.cmdbuild.logic.taskmanager.task.email.mapper.NullMapperEngine;
import org.joda.time.DateTime;

public class ReadEmailTask implements ScheduledTask {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ReadEmailTask> {

		private static final Iterable<String> EMPTY_FILTER = Collections.emptyList();
		private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

		private Long id;
		private String description;
		private Boolean active;
		private String cronExpression;
		private DateTime lastExecution;
		private String emailAccount;
		private String incomingFolder;
		private String processedFolder;
		private String rejectedFolder;
		private Boolean rejectNotMatching;
		private String filterType;
		private final Collection<String> regexFromFilter = newArrayList();
		private final Collection<String> regexSubjectFilter = newArrayList();
		private String filterFunction;
		private Boolean notificationActive;
		private String notificationTemplate;
		private Boolean attachmentsActive;
		private String attachmentsCategory;
		private Boolean workflowActive;
		private String workflowClassName;
		private final Map<String, String> workflowAttributes = newHashMap();
		private Boolean workflowAdvanceable;
		private Boolean workflowAttachments;
		private String workflowAttachmentsCategory;
		private MapperEngine mapper;

		private Builder() {
			// use factory method
		}

		@Override
		public ReadEmailTask build() {
			validate();
			return new ReadEmailTask(this);
		}

		private void validate() {
			active = defaultIfNull(active, FALSE);

			rejectNotMatching = defaultIfNull(rejectNotMatching, FALSE);

			notificationActive = defaultIfNull(notificationActive, FALSE);

			attachmentsActive = defaultIfNull(attachmentsActive, FALSE);

			workflowActive = defaultIfNull(workflowActive, FALSE);
			workflowAdvanceable = defaultIfNull(workflowAdvanceable, FALSE);
			workflowAttachments = defaultIfNull(workflowAttachments, FALSE);

			mapper = defaultIfNull(mapper, NullMapperEngine.getInstance());
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final Boolean active) {
			this.active = active;
			return this;
		}

		public Builder withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder withLastExecution(final DateTime lastExecution) {
			this.lastExecution = lastExecution;
			return this;
		}

		public Builder withEmailAccount(final String emailAccount) {
			this.emailAccount = emailAccount;
			return this;
		}

		public Builder withIncomingFolder(final String incomingFolder) {
			this.incomingFolder = incomingFolder;
			return this;
		}

		public Builder withProcessedFolder(final String processedFolder) {
			this.processedFolder = processedFolder;
			return this;
		}

		public Builder withRejectedFolder(final String rejectedFolder) {
			this.rejectedFolder = rejectedFolder;
			return this;
		}

		public Builder withRejectNotMatching(final Boolean rejectNotMatching) {
			this.rejectNotMatching = rejectNotMatching;
			return this;
		}

		public Builder withFilterType(final String filterType) {
			this.filterType = filterType;
			return this;
		}

		public Builder withRegexFromFilter(final Iterable<String> regexFromFilter) {
			addAll(this.regexFromFilter, defaultIfNull(regexFromFilter, EMPTY_FILTER));
			return this;
		}

		public Builder withRegexSubjectFilter(final Iterable<String> regexSubjectFilter) {
			addAll(this.regexSubjectFilter, defaultIfNull(regexSubjectFilter, EMPTY_FILTER));
			return this;
		}

		public Builder withFilterFunction(final String filterFunction) {
			this.filterFunction = filterFunction;
			return this;
		}

		public Builder withNotificationStatus(final Boolean notificationActive) {
			this.notificationActive = notificationActive;
			return this;
		}

		public Builder withNotificationTemplate(final String notificationTemplate) {
			this.notificationTemplate = notificationTemplate;
			return this;
		}

		public Builder withAttachmentsActive(final Boolean attachmentsActive) {
			this.attachmentsActive = attachmentsActive;
			return this;
		}

		public Builder withAttachmentsCategory(final String category) {
			this.attachmentsCategory = category;
			return this;
		}

		public Builder withWorkflowActive(final Boolean workflowActive) {
			this.workflowActive = workflowActive;
			return this;
		}

		public Builder withWorkflowClassName(final String workflowClassName) {
			this.workflowClassName = workflowClassName;
			return this;
		}

		public Builder withWorkflowAttributes(final Map<String, String> workflowAttributes) {
			this.workflowAttributes.putAll(defaultIfNull(workflowAttributes, EMPTY_ATTRIBUTES));
			return this;
		}

		public Builder withWorkflowAdvanceableStatus(final Boolean workflowAdvanceable) {
			this.workflowAdvanceable = workflowAdvanceable;
			return this;
		}

		public Builder withWorkflowAttachmentsStatus(final Boolean workflowAttachments) {
			this.workflowAttachments = workflowAttachments;
			return this;
		}

		public Builder withWorkflowAttachmentsCategory(final String workflowAttachmentsCategory) {
			this.workflowAttachmentsCategory = workflowAttachmentsCategory;
			return this;
		}

		public Builder withMapperEngine(final MapperEngine mapper) {
			this.mapper = mapper;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final String cronExpression;
	private final DateTime lastExecution;
	private final String emailAccount;
	private final String incomingFolder;
	private final String processedFolder;
	private final String rejectedFolder;
	private final boolean rejectNotMatching;
	private final String filterType;
	private final Iterable<String> regexFromFilter;
	private final Iterable<String> regexSubjectFilter;
	private final String filterFunction;
	private final boolean notificationActive;
	private final String notificationTemplate;
	private final boolean attachmentsActive;
	private final String attachmentsCategory;
	private final boolean workflowActive;
	private final String workflowClassName;
	private final Map<String, String> workflowAttributes;
	private final boolean workflowAdvanceable;
	private final boolean workflowAttachments;
	private final String workflowAttachmentsCategory;
	private final MapperEngine mapper;

	private ReadEmailTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.lastExecution = builder.lastExecution;
		this.emailAccount = builder.emailAccount;
		this.incomingFolder = builder.incomingFolder;
		this.processedFolder = builder.processedFolder;
		this.rejectedFolder = builder.rejectedFolder;
		this.rejectNotMatching = builder.rejectNotMatching;
		this.filterType = builder.filterType;
		this.regexFromFilter = builder.regexFromFilter;
		this.regexSubjectFilter = builder.regexSubjectFilter;
		this.filterFunction = builder.filterFunction;
		this.notificationActive = builder.notificationActive;
		this.notificationTemplate = builder.notificationTemplate;
		this.attachmentsActive = builder.attachmentsActive;
		this.attachmentsCategory = builder.attachmentsCategory;
		this.workflowActive = builder.workflowActive;
		this.workflowClassName = builder.workflowClassName;
		this.workflowAttributes = builder.workflowAttributes;
		this.workflowAdvanceable = builder.workflowAdvanceable;
		this.workflowAttachments = builder.workflowAttachments;
		this.workflowAttachmentsCategory = builder.workflowAttachmentsCategory;
		this.mapper = builder.mapper;
	}

	@Override
	public void accept(final TaskVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	@Override
	public boolean isExecutable() {
		return true;
	}

	@Override
	public DateTime getLastExecution() {
		return lastExecution;
	}

	public String getEmailAccount() {
		return emailAccount;
	}

	public String getIncomingFolder() {
		return incomingFolder;
	}

	public String getProcessedFolder() {
		return processedFolder;
	}

	public String getRejectedFolder() {
		return rejectedFolder;
	}

	public boolean isRejectNotMatching() {
		return rejectNotMatching;
	}

	public boolean isNotificationActive() {
		return notificationActive;
	}

	public String getNotificationTemplate() {
		return notificationTemplate;
	}

	public String getFilterType() {
		return filterType;
	}

	public Iterable<String> getRegexFromFilter() {
		return regexFromFilter;
	}

	public Iterable<String> getRegexSubjectFilter() {
		return regexSubjectFilter;
	}

	public String getFilterFunction() {
		return filterFunction;
	}

	public boolean isAttachmentsActive() {
		return attachmentsActive;
	}

	public String getAttachmentsCategory() {
		return attachmentsCategory;
	}

	public boolean isWorkflowActive() {
		return workflowActive;
	}

	public String getWorkflowClassName() {
		return workflowClassName;
	}

	public Map<String, String> getWorkflowAttributes() {
		return workflowAttributes;
	}

	public boolean isWorkflowAdvanceable() {
		return workflowAdvanceable;
	}

	public boolean isWorkflowAttachments() {
		return workflowAttachments;
	}

	public String getWorkflowAttachmentsCategory() {
		return workflowAttachmentsCategory;
	}

	public MapperEngine getMapperEngine() {
		return mapper;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
