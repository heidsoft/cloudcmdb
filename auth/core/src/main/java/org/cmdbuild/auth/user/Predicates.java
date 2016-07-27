package org.cmdbuild.auth.user;

import com.google.common.base.Predicate;

public class Predicates {

	private Predicates() {
		// prevents instantiation
	}

	private static final Predicate<CMUser> PRIVILEGED = new Predicate<CMUser>() {

		@Override
		public boolean apply(final CMUser input) {
			return input.isPrivileged();
		}

	};

	public static Predicate<CMUser> privileged() {
		return PRIVILEGED;
	}

}
