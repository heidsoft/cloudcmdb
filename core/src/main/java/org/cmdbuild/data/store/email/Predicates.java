package org.cmdbuild.data.store.email;

import com.google.common.base.Predicate;

public class Predicates {

	public static Predicate<EmailAccount> named(final String name) {
		return new Predicate<EmailAccount>() {

			@Override
			public boolean apply(final EmailAccount input) {
				return input.getName().equals(name);
			}

		};
	}

	public static Predicate<EmailAccount> isDefault() {
		return new Predicate<EmailAccount>() {

			@Override
			public boolean apply(final EmailAccount input) {
				return input.isDefault();
			}

		};
	}

	private Predicates() {
		// prevents instantiation
	}

}
