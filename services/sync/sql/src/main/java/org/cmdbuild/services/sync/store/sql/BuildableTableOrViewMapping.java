package org.cmdbuild.services.sync.store.sql;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BuildableTableOrViewMapping implements TableOrViewMapping {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<BuildableTableOrViewMapping> {

		private static final Iterable<TypeMapping> NO_MAPPINGS = Collections.emptyList();

		private String name;
		private final Collection<TypeMapping> typeMappings = newHashSet();

		@Override
		public BuildableTableOrViewMapping build() {
			validate();
			return new BuildableTableOrViewMapping(this);
		}

		private void validate() {
			Validate.notBlank(name, "invalid name '%s'", name);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withTypeMappings(final Iterable<? extends TypeMapping> typeMappings) {
			addAll(this.typeMappings, defaultIfNull(typeMappings, NO_MAPPINGS));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String name;
	private final Iterable<TypeMapping> typeMappings;

	private BuildableTableOrViewMapping(final Builder builder) {
		this.name = builder.name;
		this.typeMappings = builder.typeMappings;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Iterable<TypeMapping> getTypeMappings() {
		return typeMappings;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
