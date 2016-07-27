package org.cmdbuild.services.sync.store.sql;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.services.sync.store.ClassType;

public class BuildableTypeMapper implements TypeMapping {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<TypeMapping> {

		private static final Iterable<AttributeMapping> NO_MAPPERS = Collections.emptyList();

		private ClassType type;
		private final Collection<AttributeMapping> attributeMappings = newHashSet();

		private Builder() {
			// use factory method
		}

		@Override
		public BuildableTypeMapper build() {
			validate();
			return new BuildableTypeMapper(this);
		}

		private void validate() {
			Validate.notNull(type, "missing '%s'", type.getClass());
		}

		public Builder withType(final ClassType type) {
			this.type = type;
			return this;
		}

		public Builder withAttributeMapper(final AttributeMapping attributeMapping) {
			this.attributeMappings.add(attributeMapping);
			return this;
		}

		public Builder withAttributeMappers(final Iterable<? extends AttributeMapping> attributeMappers) {
			addAll(this.attributeMappings, defaultIfNull(attributeMappers, NO_MAPPERS));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final ClassType type;
	private final Iterable<AttributeMapping> attributeMappings;
	private final int hashCode;

	private BuildableTypeMapper(final Builder builder) {
		this.type = builder.type;
		this.attributeMappings = builder.attributeMappings;
		this.hashCode = new HashCodeBuilder() //
				.append(this.type) //
				.append(this.attributeMappings) //
				.toHashCode();
	}

	@Override
	public ClassType getType() {
		return type;
	}

	@Override
	public Iterable<AttributeMapping> getAttributeMappings() {
		return attributeMappings;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof BuildableTypeMapper)) {
			return false;
		}
		final BuildableTypeMapper other = BuildableTypeMapper.class.cast(obj);
		return new EqualsBuilder() //
				.append(type, other.type) //
				.append(attributeMappings, other.attributeMappings) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
