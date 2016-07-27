package org.cmdbuild.common.utils.guava;

import com.google.common.base.Supplier;

public class Suppliers {

	private static class NullOnException<T> implements Supplier<T> {

		private final Supplier<T> delegate;

		public NullOnException(final Supplier<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T get() {
			try {
				return delegate.get();
			} catch (final Exception e) {
				return null;
			}
		}

	}

	public static <T> Supplier<T> nullOnException(final Supplier<T> delegate) {
		return new NullOnException<T>(delegate);
	}

	private static class FirstNotNull<T> implements Supplier<T> {

		private final Iterable<? extends Supplier<T>> delegates;

		public FirstNotNull(final Iterable<? extends Supplier<T>> delegates) {
			this.delegates = delegates;
		}

		@Override
		public T get() {
			for (final Supplier<T> delegate : delegates) {
				final T value = delegate.get();
				if (value != null) {
					return value;
				}
			}
			return null;
		}

	}

	public static <T> Supplier<T> firstNotNull(final Iterable<? extends Supplier<T>> delegates) {
		return new FirstNotNull<T>(delegates);
	}

	private Suppliers() {
		// prevents instantiation
	}

}
