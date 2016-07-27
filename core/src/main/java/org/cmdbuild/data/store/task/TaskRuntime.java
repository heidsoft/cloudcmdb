package org.cmdbuild.data.store.task;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Storable;
import org.joda.time.DateTime;

public class TaskRuntime implements Storable {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<TaskRuntime> {

		private Long id;
		private Long owner;
		private DateTime lastExecution;

		private Builder() {
		}

		@Override
		public TaskRuntime build() {
			validate();
			return new TaskRuntime(this);
		}

		private void validate() {
			Validate.notNull(owner, "invalid owner");
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withOwner(final Long owner) {
			this.owner = owner;
			return this;
		}

		public Builder withLastExecution(final DateTime lastExecution) {
			this.lastExecution = lastExecution;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final Long owner;
	private final DateTime lastExecution;

	private TaskRuntime(final Builder builder) {
		this.id = builder.id;
		this.owner = builder.owner;
		this.lastExecution = builder.lastExecution;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public Long getId() {
		return id;
	}

	public Long getOwner() {
		return owner;
	}

	public DateTime getLastExecution() {
		return lastExecution;
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
