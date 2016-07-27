package org.cmdbuild.data.store.task;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public abstract class Task implements Storable {

	public static abstract class Builder<T extends Task> implements org.apache.commons.lang3.builder.Builder<T> {

		private static final Map<String, String> NO_PARAMETERS = Collections.emptyMap();

		private Long id;
		private String description;
		private Boolean running;
		private String cronExpression;
		private DateTime lastExecution;
		private final Map<String, String> parameters = Maps.newHashMap();

		protected Builder() {
			// usable by subclasses only
		}

		@Override
		public T build() {
			validate();
			return doBuild();
		}

		private void validate() {
			running = (running == null) ? Boolean.FALSE : running;
		}

		protected abstract T doBuild();

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		public Builder<T> withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder<T> withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder<T> withRunningStatus(final Boolean running) {
			this.running = running;
			return this;
		}

		public Builder<T> withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder<T> withLastExecution(final DateTime lastExecution) {
			this.lastExecution = lastExecution;
			return this;
		}

		public Builder<T> withParameters(final Map<String, ? extends String> parameters) {
			this.parameters.putAll(defaultIfNull(parameters, NO_PARAMETERS));
			return this;
		}

		public Builder<T> withParameter(final String key, final String value) {
			this.parameters.put(key, value);
			return this;
		}

	}

	private final Long id;
	private final String description;
	private final boolean running;
	private final DateTime lastExecution;
	private final String cronExpression;
	private final Map<String, String> parameters;

	protected Task(final Builder<? extends Task> builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.running = builder.running;
		this.cronExpression = builder.cronExpression;
		this.lastExecution = builder.lastExecution;
		this.parameters = builder.parameters;
	}

	public abstract void accept(final TaskVisitor visitor);

	public Builder<? extends Task> modify() {
		return builder() //
				.withId(id) //
				.withDescription(description) //
				.withRunningStatus(running) //
				.withLastExecution(lastExecution) //
				.withCronExpression(cronExpression) //
				.withParameters(parameters);
	}

	protected abstract Builder<? extends Task> builder();

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRunning() {
		return running;
	}

	// TODO move to parameters
	public String getCronExpression() {
		return cronExpression;
	}

	// TODO move some where else
	public DateTime getLastExecution() {
		return lastExecution;
	}

	// TODO use something different from Map
	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getParameter(final String key) {
		return parameters.get(key);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Task)) {
			return false;
		}
		final Task other = Task.class.cast(obj);
		return new EqualsBuilder() //
				.append(id, other.id) //
				.append(description, other.description) //
				.append(running, other.running) //
				.append(cronExpression, other.cronExpression) //
				.append(parameters, other.parameters) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(id) //
				.append(description) //
				.append(running) //
				.append(cronExpression) //
				.append(parameters) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
