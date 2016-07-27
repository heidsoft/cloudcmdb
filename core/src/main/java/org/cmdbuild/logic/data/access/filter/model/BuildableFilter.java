package org.cmdbuild.logic.data.access.filter.model;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Optional;

public class BuildableFilter implements Filter {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<Filter> {

		private static final Optional<Element> ABSENT_ATTRIBUTE = Optional.absent();
		private static final Optional<String> ABSENT_STRING = Optional.absent();

		private Optional<Element> attribute;
		private Optional<String> fullTextQuery;

		private Builder() {
			// use factory method
		}

		@Override
		public Filter build() {
			validate();
			return new BuildableFilter(this);
		}

		private void validate() {
			attribute = defaultIfNull(attribute, ABSENT_ATTRIBUTE);
			fullTextQuery = defaultIfNull(fullTextQuery, ABSENT_STRING);
		}

		public Builder withAttribute(final Element attribute) {
			this.attribute = (attribute == null) ? ABSENT_ATTRIBUTE : Optional.of(attribute);
			return this;
		}

		public Builder withFullTextQuery(final String fullTextQuery) {
			this.fullTextQuery = (fullTextQuery == null) ? ABSENT_STRING : Optional.of(fullTextQuery);
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Optional<Element> attribute;
	private final Optional<String> fullTextQuery;

	private BuildableFilter(final Builder builder) {
		this.attribute = builder.attribute;
		this.fullTextQuery = builder.fullTextQuery;
	}

	@Override
	public Optional<Element> attribute() {
		return attribute;
	}

	@Override
	public Optional<String> fullTextQuery() {
		return fullTextQuery;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof BuildableFilter)) {
			return false;
		}
		final BuildableFilter other = BuildableFilter.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.attribute, other.attribute) //
				.append(this.fullTextQuery, other.fullTextQuery) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(attribute) //
				.append(fullTextQuery) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
