package org.cmdbuild.services.sync.store;

import com.google.common.base.Predicate;

class Predicates {

	private static Predicate<Attribute> KEY_ATTRIBUTES = new Predicate<Attribute>() {

		@Override
		public boolean apply(final Attribute input) {
			return input.isKey();
		}

	};

	public static Predicate<Attribute> keyAttributes() {
		return KEY_ATTRIBUTES;
	}

	private Predicates() {
		// prevents instantiation
	}

}
