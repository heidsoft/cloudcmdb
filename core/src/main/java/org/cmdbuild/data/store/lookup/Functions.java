package org.cmdbuild.data.store.lookup;

import com.google.common.base.Function;

public class Functions {

	private static final Function<Lookup, Long> LOOKUP_ID = new Function<Lookup, Long>() {

		@Override
		public Long apply(final Lookup input) {
			return input.getId();
		}

	};

	private static final Function<Lookup, LookupType> LOOKUP_TYPE = new Function<Lookup, LookupType>() {

		@Override
		public LookupType apply(final Lookup input) {
			return input.type();
		}

	};

	private static final Function<Lookup, String> LOOKUP_TRANSLATION_UUID = new Function<Lookup, String>() {

		@Override
		public String apply(final Lookup input) {
			return input.getTranslationUuid();
		}

	};

	public static Function<Lookup, Long> toLookupId() {
		return LOOKUP_ID;
	}

	public static Function<Lookup, LookupType> toLookupType() {
		return LOOKUP_TYPE;
	}

	public static Function<Lookup, String> toTranslationUuid() {
		return LOOKUP_TRANSLATION_UUID;
	}

	private Functions() {
		// prevents instantiation
	}

}
