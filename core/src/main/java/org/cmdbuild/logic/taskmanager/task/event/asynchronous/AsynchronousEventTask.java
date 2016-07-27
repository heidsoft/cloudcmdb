package org.cmdbuild.logic.taskmanager.task.event.asynchronous;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.TaskVisitor;
import org.joda.time.DateTime;

public class AsynchronousEventTask implements ScheduledTask {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AsynchronousEventTask> {

		private Long id;
		private String description;
		private Boolean active;
		private String cronExpression;
		private DateTime lastExecution;
		private String classname;
		private String filter;
		private Boolean notificationActive;
		private String notificationAcccount;
		private String notificationTemplate;

		private Builder() {
			// use factory method
		}

		@Override
		public AsynchronousEventTask build() {
			validate();
			return new AsynchronousEventTask(this);
		}

		private void validate() {
			active = defaultIfNull(active, FALSE);
			notificationActive = defaultIfNull(notificationActive, FALSE);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final boolean active) {
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

		public Builder withTargetClass(final String classname) {
			this.classname = classname;
			return this;
		}

		public Builder withFilter(final String filter) {
			this.filter = filter;
			return this;
		}

		public Builder withNotificationStatus(final Boolean notificationActive) {
			this.notificationActive = notificationActive;
			return this;
		}

		public Builder withNotificationAccount(final String notificationAcccount) {
			this.notificationAcccount = notificationAcccount;
			return this;
		}

		public Builder withNotificationErrorTemplate(final String notificationTemplate) {
			this.notificationTemplate = notificationTemplate;
			return this;
		}

	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final String cronExpression;
	private final DateTime lastExecution;
	private final String classname;
	private final String filter;
	private final boolean notificationActive;
	private final String notificationAcccount;
	private final String notificationTemplate;

	private AsynchronousEventTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.lastExecution = builder.lastExecution;
		this.classname = builder.classname;
		this.filter = builder.filter;
		this.notificationActive = builder.notificationActive;
		this.notificationAcccount = builder.notificationAcccount;
		this.notificationTemplate = builder.notificationTemplate;
	}

	public static Builder newInstance() {
		return new Builder();
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
	public boolean isExecutable() {
		return true;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	@Override
	public DateTime getLastExecution() {
		return lastExecution;
	}

	public String getTargetClassname() {
		return classname;
	}

	public String getFilter() {
		return filter;
	}

	public boolean isNotificationActive() {
		return notificationActive;
	}

	public String getNotificationAccount() {
		return notificationAcccount;
	}

	public String getNotificationTemplate() {
		return notificationTemplate;
	}

}
