package org.cmdbuild.data.store.lookup;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;

public class Predicates {

	private static abstract class LookupPredicate<T> extends ForwardingObject implements Predicate<Lookup> {

		/**
		 * Usable by subclasses only.
		 */
		protected LookupPredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(Lookup input);

		@Override
		public final boolean apply(final Lookup input) {
			return delegate().apply(value(input));
		}

	}

	private static class LookupId extends LookupPredicate<Long> {

		private final Predicate<Long> delegate;

		public LookupId(final Predicate<Long> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Long> delegate() {
			return delegate;
		}

		@Override
		protected Long value(final Lookup input) {
			return input.getId();
		}

	}

	public static Predicate<Lookup> lookupId(final Predicate<Long> delegate) {
		return new LookupId(delegate);
	}

	private static class LookupTranslationUuid extends LookupPredicate<String> {

		private final Predicate<String> delegate;

		public LookupTranslationUuid(final Predicate<String> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<String> delegate() {
			return delegate;
		}

		@Override
		protected String value(final Lookup input) {
			return input.getTranslationUuid();
		}

	}

	public static Predicate<Lookup> lookupTranslationUuid(final Predicate<String> delegate) {
		return new LookupTranslationUuid(delegate);
	}

	private static final Predicate<Lookup> ACTIVE = new Predicate<Lookup>() {

		@Override
		public boolean apply(final Lookup input) {
			return input.active();
		}

	};

	private static final Predicate<Lookup> DEFAULT = new Predicate<Lookup>() {

		@Override
		public boolean apply(final Lookup input) {
			return input.isDefault();
		}

	};

	public static Predicate<Lookup> lookupActive() {
		return ACTIVE;
	}

	public static Predicate<Lookup> lookupWithType(final LookupType type) {
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return input.type().name.equals(type.name);
			}

		};
	}

	public static Predicate<Lookup> lookupWithDescription(final String description) {
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return input.description().equals(description);
			}

		};
	}

	public static Predicate<LookupType> lookupTypeWithName(final String name) {
		return new Predicate<LookupType>() {

			@Override
			public boolean apply(final LookupType input) {
				return input.name.equals(name);
			}

		};
	}

	public static Predicate<Lookup> defaultLookup() {
		return DEFAULT;
	}

	private Predicates() {
		// prevents instantiation
	}

}