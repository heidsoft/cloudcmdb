package org.cmdbuild.logic.icon;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Types {

	private static abstract class AbstractType implements Type {

		/**
		 * Usable by subclasses only.
		 */
		protected AbstractType() {
		}

		@Override
		public final boolean equals(final Object obj) {
			return doEquals(obj);
		}

		protected abstract boolean doEquals(Object obj);

		@Override
		public final int hashCode() {
			return doHashCode();
		}

		protected abstract int doHashCode();

		@Override
		public final String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static class ClassType extends AbstractType {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<ClassType> {

			private String name;

			private Builder() {
				// use factory method
			}

			@Override
			public ClassType build() {
				return new ClassType(this);
			}

			public Builder withName(final String value) {
				name = value;
				return this;
			}

		}

		private final String name;

		private ClassType(final Builder builder) {
			this.name = requireNonNull(builder.name);
		}

		public String getName() {
			return name;
		}

		@Override
		public void accept(final TypeVisitor visitor) {
			visitor.visit(this);
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof ClassType)) {
				return false;
			}

			final ClassType other = ClassType.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getName(), other.getName()) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(getName()) //
					.toHashCode();
		}

	}

	public static ClassType.Builder classType() {
		return new ClassType.Builder();
	}

	public static class ProcessType extends AbstractType {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<ProcessType> {

			private String name;

			private Builder() {
				// use factory method
			}

			@Override
			public ProcessType build() {
				return new ProcessType(this);
			}

			public Builder withName(final String value) {
				name = value;
				return this;
			}

		}

		private final String name;

		private ProcessType(final Builder builder) {
			this.name = requireNonNull(builder.name);
		}

		public String getName() {
			return name;
		}

		@Override
		public void accept(final TypeVisitor visitor) {
			visitor.visit(this);
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof ProcessType)) {
				return false;
			}

			final ProcessType other = ProcessType.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getName(), other.getName()) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(getName()) //
					.toHashCode();
		}

	}

	public static ProcessType.Builder processType() {
		return new ProcessType.Builder();
	}

	private Types() {
		// prevents instantiation
	}

}
