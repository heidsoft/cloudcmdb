package org.cmdbuild.dao.entrytype;

import com.google.common.base.Predicate;

public interface Deactivable {

	boolean isActive();

	class IsActivePredicate implements Predicate<Deactivable> {

		private static final IsActivePredicate INSTANCE = new IsActivePredicate();

		@Override
		public boolean apply(final Deactivable input) {
			return input.isActive();
		}

		public static final IsActivePredicate activeOnes() {
			return INSTANCE;
		}

	}

}
