package org.cmdbuild.model.widget;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;

public class Predicates {

	private static abstract class WidgetPredicate<T> extends ForwardingObject implements Predicate<Widget> {
		
		/**
		 * Usable by subclasses only.
		 */
		protected WidgetPredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		protected abstract T value(Widget input);

		@Override
		public final boolean apply(final Widget input) {
			return delegate().apply(value(input));
		}

	}

	private static class Active extends WidgetPredicate<Boolean> {

		private final Predicate<Boolean> delegate;

		private Active(final Predicate<Boolean> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<Boolean> delegate() {
			return delegate;
		}

		@Override
		protected Boolean value(final Widget input) {
			return input.isActive();
		}

	}

	public static Predicate<Widget> active(final Predicate<Boolean> delegate) {
		return new Active(delegate);
	}

	private static class SourceClass extends WidgetPredicate<String> {

		private final Predicate<String> delegate;

		private SourceClass(final Predicate<String> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<String> delegate() {
			return delegate;
		}

		@Override
		protected String value(final Widget input) {
			return input.getSourceClass();
		}

	}

	public static Predicate<Widget> sourceClass(final Predicate<String> delegate) {
		return new SourceClass(delegate);
	}

	private Predicates() {
		// prevents instantiation
	}

}
