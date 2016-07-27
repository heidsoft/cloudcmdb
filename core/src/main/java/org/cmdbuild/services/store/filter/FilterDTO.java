package org.cmdbuild.services.store.filter;

import static com.google.common.reflect.Reflection.newProxy;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.common.utils.Reflection.unsupported;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.services.localization.LocalizableStorableVisitor;
import org.cmdbuild.services.store.filter.FilterStore.Filter;

public class FilterDTO extends ForwardingFilter {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<FilterDTO> {

		private Long id;
		private String name;
		private String description;
		private String configuration;
		private String className;
		private boolean shared;
		private Long userId;

		/**
		 * Use factory method.
		 */
		private Builder() {
		}

		@Override
		public FilterDTO build() {
			shared = isTrue(shared);
			return new FilterDTO(this);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withClassName(final String className) {
			this.className = className;
			return this;
		}

		public Builder withConfiguration(final String configuration) {
			this.configuration = configuration;
			return this;
		}

		public Builder thatIsShared(final Boolean shared) {
			this.shared = shared;
			return this;
		}

		public Builder withUserId(final Long userId) {
			this.userId = userId;
			return this;
		}

	}

	public static Builder newFilter() {
		return new Builder();
	}

	private static final Filter unsupported = newProxy(Filter.class, unsupported("should be never called"));

	private final Long id;
	private final String name;
	private final String description;
	private final String configuration;
	private final String className;
	private final boolean shared;
	private final Long userId;

	private FilterDTO(final Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.description = builder.description;
		this.className = builder.className;
		this.configuration = builder.configuration;
		this.shared = builder.shared;
		this.userId = builder.userId;
	}

	@Override
	protected Filter delegate() {
		return unsupported;
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getPrivilegeId() {
		return String.format("Filter:%d", getId());
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getConfiguration() {
		return configuration;
	}

	@Override
	public boolean isShared() {
		return shared;
	}

	@Override
	public Long getUserId() {
		return userId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Filter)) {
			return false;
		}
		final Filter other = Filter.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.getName(), other.getName()) //
				.append(this.getDescription(), other.getDescription()) //
				.append(this.isShared(), other.isShared()) //
				.append(this.getClassName(), other.getClassName()) //
				.append(this.getConfiguration(), other.getConfiguration()) //
				.append(this.getUserId(), other.getUserId()) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(getName()) //
				.append(getDescription()) //
				.append(isShared()) //
				.append(getClassName()) //
				.append(getConfiguration()) //
				.append(getUserId()) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
