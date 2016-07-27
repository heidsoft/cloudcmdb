package org.cmdbuild.services.sync.store.internal;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.services.sync.store.Type;

import com.google.common.base.Predicate;

public class BuildableCatalog implements Catalog {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<BuildableCatalog> {

		private static final Iterable<? extends Type> NO_TYPES = Collections.emptyList();

		private final Collection<Type> types = newHashSet();

		private Builder() {
			// use factory method
		}

		@Override
		public BuildableCatalog build() {
			validate();
			return new BuildableCatalog(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withTypes(final Iterable<? extends Type> types) {
			addAll(this.types, from(defaultIfNull(types, NO_TYPES)).filter(notNull()));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Iterable<Type> types;

	private BuildableCatalog(final Builder builder) {
		this.types = builder.types;
	}

	@Override
	public Iterable<Type> getTypes() {
		return types;
	}

	@Override
	public <T extends Type> T getType(final String name, final Class<T> type) {
		return from(types) //
				.filter(type) //
				.firstMatch(new Predicate<Type>() {

					@Override
					public boolean apply(final Type input) {
						return input.getName().equals(name);
					}

				}) //
				.orNull();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Catalog)) {
			return false;
		}
		final Catalog other = Catalog.class.cast(obj);
		return new EqualsBuilder() //
				.append(getTypes(), other.getTypes()) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(types) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE) //
				.append(types) //
				.toString();
	}

}
