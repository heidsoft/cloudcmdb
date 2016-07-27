package org.cmdbuild.dao.entrytype;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.Iterables.contains;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;

public class Predicates {

	private static class AttributeTypeIsInstanceOf implements Predicate<CMAttribute> {

		private final Class<? extends CMAttributeType<?>> clazz;

		private AttributeTypeIsInstanceOf(final Class<? extends CMAttributeType<?>> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean apply(final CMAttribute input) {
			return (input == null) ? false : clazz.isInstance(input.getType());
		};

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AttributeTypeIsInstanceOf)) {
				return false;
			}
			final AttributeTypeIsInstanceOf other = AttributeTypeIsInstanceOf.class.cast(obj);
			return (this.clazz == other.clazz);
		}

		@Override
		public int hashCode() {
			return clazz.hashCode();
		}

		@Override
		public String toString() {
			return this.clazz.getSimpleName() + "(" + clazz.getName() + ")";
		}

	}

	public static Predicate<CMAttribute> attributeTypeInstanceOf(final Class<? extends CMAttributeType<?>> clazz) {
		return new AttributeTypeIsInstanceOf(clazz);
	}

	private static class DomainForClass implements Predicate<CMDomain> {

		private final CMClass target;

		private DomainForClass(final CMClass target) {
			this.target = target;
		}

		@Override
		public boolean apply(final CMDomain input) {
			return input.getClass1().isAncestorOf(target) || input.getClass2().isAncestorOf(target);
		};

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DomainForClass)) {
				return false;
			}
			final DomainForClass other = DomainForClass.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.target, other.target) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(target) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<CMDomain> domainFor(final CMClass target) {
		return new DomainForClass(target);
	}

	private static class DisabledClass implements Predicate<CMDomain> {

		private final String target;

		private DisabledClass(final CMClass target) {
			this.target = target.getName();
		}

		@Override
		public boolean apply(final CMDomain input) {
			return contains(input.getDisabled1(), target) || contains(input.getDisabled2(), target);
		};

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DisabledClass)) {
				return false;
			}
			final DisabledClass other = DisabledClass.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.target, other.target) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(target) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<CMDomain> disabledClass(final CMClass target) {
		return new DisabledClass(target);
	}

	private static class UsableForReferences implements Predicate<CMDomain> {

		private final CMClass target;

		private UsableForReferences(final CMClass target) {
			this.target = target;
		}

		@Override
		public boolean apply(final CMDomain input) {
			final String cardinality = input.getCardinality();
			if (cardinality.equals(CARDINALITY_1N.value()) && input.getClass2().isAncestorOf(target)) {
				return true;
			} else if (cardinality.equals(CARDINALITY_N1.value()) && input.getClass1().isAncestorOf(target)) {
				return true;
			}
			return false;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof DomainForClass)) {
				return false;
			}
			final DomainForClass other = DomainForClass.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.target, other.target) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(target) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<CMDomain> usableForReferences(final CMClass target) {
		return new UsableForReferences(target);
	}

	private static class IsSystem<T extends CMEntryType> implements Predicate<T> {

		private final Class<? extends CMEntryType> type;

		private IsSystem(final Class<? extends CMEntryType> type) {
			this.type = type;
		}

		@Override
		public boolean apply(final T input) {
			return input.isSystem();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof IsSystem)) {
				return false;
			}
			final IsSystem<?> other = IsSystem.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.type, other.type) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(type) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<CMDomain> isSystem(final Class<? extends CMEntryType> type) {
		return new IsSystem(type);
	}

	public static Predicate<CMDomain> allDomains() {
		return alwaysTrue();
	}

	private static abstract class AttributePredicate<T> extends ForwardingObject implements Predicate<CMAttribute> {

		/**
		 * Usable by subclasses only.
		 */
		protected AttributePredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(CMAttribute input);

		@Override
		public final boolean apply(final CMAttribute input) {
			return delegate().apply(value(input));
		}

	}

	private static class Name extends AttributePredicate<String> {

		private final Predicate<String> delegate;

		public Name(final Predicate<String> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<String> delegate() {
			return delegate;
		}

		@Override
		protected String value(final CMAttribute input) {
			return input.getName();
		}

	}

	public static Predicate<CMAttribute> name(final Predicate<String> delegate) {
		return new Name(delegate);
	}

	private static class ClassOrder extends AttributePredicate<Integer> {

		private final Predicate<Integer> delegate;

		public ClassOrder(final Predicate<Integer> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Integer> delegate() {
			return delegate;
		}

		@Override
		protected Integer value(final CMAttribute input) {
			return input.getClassOrder();
		}

	}

	public static Predicate<CMAttribute> classOrder(final Predicate<Integer> delegate) {
		return new ClassOrder(delegate);
	}

	private static class Mode extends AttributePredicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> {

		private final Predicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> delegate;

		public Mode(final Predicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> delegate() {
			return delegate;
		}

		@Override
		protected org.cmdbuild.dao.entrytype.CMAttribute.Mode value(final CMAttribute input) {
			return input.getMode();
		}

	}

	public static Predicate<CMAttribute> mode(final Predicate<org.cmdbuild.dao.entrytype.CMAttribute.Mode> delegate) {
		return new Mode(delegate);
	}

	private static abstract class FunctionPredicate<T> extends ForwardingObject implements Predicate<CMFunction> {

		/**
		 * Usable by subclasses only.
		 */
		protected FunctionPredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(CMFunction input);

		@Override
		public final boolean apply(final CMFunction input) {
			return delegate().apply(value(input));
		}

	}

	private static class FunctionId extends FunctionPredicate<Long> {

		private final Predicate<Long> delegate;

		public FunctionId(final Predicate<Long> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Long> delegate() {
			return delegate;
		}

		@Override
		protected Long value(final CMFunction input) {
			return input.getId();
		}

	}

	public static Predicate<CMFunction> functionId(final Predicate<Long> delegate) {
		return new FunctionId(delegate);
	}

	private static abstract class FunctionParameterPredicate<T> extends ForwardingObject
			implements Predicate<CMFunctionParameter> {

		/**
		 * Usable by subclasses only.
		 */
		protected FunctionParameterPredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(CMFunctionParameter input);

		@Override
		public final boolean apply(final CMFunctionParameter input) {
			return delegate().apply(value(input));
		}

	}

	private static class FunctionParameterName extends FunctionParameterPredicate<String> {

		private final Predicate<String> delegate;

		public FunctionParameterName(final Predicate<String> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<String> delegate() {
			return delegate;
		}

		@Override
		protected String value(final CMFunctionParameter input) {
			return input.getName();
		}

	}

	public static Predicate<CMFunctionParameter> parameterName(final Predicate<String> delegate) {
		return new FunctionParameterName(delegate);
	}

	private static abstract class EntryTypePredicate<T> extends ForwardingObject implements Predicate<CMEntryType> {

		/**
		 * Usable by subclasses only.
		 */
		protected EntryTypePredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(CMEntryType input);

		@Override
		public final boolean apply(final CMEntryType input) {
			return delegate().apply(value(input));
		}

	}

	private static class IsSystem_ extends EntryTypePredicate<Boolean> {

		private final Predicate<Boolean> delegate;

		public IsSystem_(final Predicate<Boolean> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Boolean> delegate() {
			return delegate;
		}

		@Override
		protected Boolean value(final CMEntryType input) {
			return input.isSystem();
		}

	}

	public static Predicate<CMEntryType> isSystem(final Predicate<Boolean> delegate) {
		return new IsSystem_(delegate);
	}

	private static class IsBaseClass extends EntryTypePredicate<Boolean> {

		private final Predicate<Boolean> delegate;

		public IsBaseClass(final Predicate<Boolean> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Boolean> delegate() {
			return delegate;
		}

		@Override
		protected Boolean value(final CMEntryType input) {
			return input.isBaseClass();
		}

	}

	public static Predicate<CMEntryType> isBaseClass(final Predicate<Boolean> delegate) {
		return new IsBaseClass(delegate);
	}

	private static class HasAncestor implements Predicate<CMClass> {

		private final CMClass anchestor;

		public HasAncestor(final CMClass anchestor) {
			this.anchestor = anchestor;
		}

		@Override
		public boolean apply(final CMClass input) {
			return anchestor.isAncestorOf(input);
		}

	}

	public static Predicate<CMClass> hasAnchestor(final CMClass value) {
		return new HasAncestor(value);
	}

	private Predicates() {
		// prevents instantiation
	}

}
