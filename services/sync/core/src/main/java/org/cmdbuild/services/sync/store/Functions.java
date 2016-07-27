package org.cmdbuild.services.sync.store;

import com.google.common.base.Function;

class Functions {

	private static Function<Attribute, String> TO_ATTRIBUTE_NAME = new Function<Attribute, String>() {

		@Override
		public String apply(final Attribute input) {
			return input.getName();
		}

	};

	private static Function<Attribute, Boolean> TO_ATTRIBUTE_KEY = new Function<Attribute, Boolean>() {

		@Override
		public Boolean apply(final Attribute input) {
			return input.isKey();
		}

	};

	public static Function<Attribute, String> toAttributeName() {
		return TO_ATTRIBUTE_NAME;
	}

	public static Function<Attribute, Boolean> toAttributeKey() {
		return TO_ATTRIBUTE_KEY;
	}

	private Functions() {
		// prevents instantiation
	}

}
