package org.cmdbuild.data.store.task;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.data.store.Storable;

public class TaskParameter implements Storable {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<TaskParameter> {

		private Long id;
		private Long owner;
		private String key;
		private String value;

		private Builder() {
			// use factory method
		}

		@Override
		public TaskParameter build() {
			validate();
			return new TaskParameter(this);
		}

		private void validate() {
			Validate.notNull(owner, "invalid owner");
			Validate.notBlank(key, "invalid key");
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

		public Builder withKey(final String key) {
			this.key = key;
			return this;
		}

		public Builder withValue(final String value) {
			this.value = value;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final Long owner;
	private final String key;
	private final String value;

	private TaskParameter(final Builder builder) {
		this.id = builder.id;
		this.owner = builder.owner;
		this.key = builder.key;
		this.value = builder.value;
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

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(id) //
				.append(owner) //
				.append(key) //
				.append(value) //
				.toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TaskParameter)) {
			return false;
		}
		final TaskParameter other = TaskParameter.class.cast(obj);
		return new EqualsBuilder() //
				.append(id, other.id) //
				.append(owner, other.owner) //
				.append(key, other.key) //
				.append(value, other.value) //
				.isEquals();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
