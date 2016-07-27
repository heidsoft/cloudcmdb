package org.cmdbuild.data.store.metadata;

import com.google.common.base.Function;

public class Functions {

	private static enum ToString implements Function<Metadata, String> {

		NAME() {

			@Override
			public String apply(final Metadata input) {
				return input.name();
			}

		},
		VALUE() {

			@Override
			public String apply(final Metadata input) {
				return input.value();
			}

		},

	}

	public static Function<Metadata, String> name() {
		return ToString.NAME;
	}

	public static Function<Metadata, String> value() {
		return ToString.VALUE;
	}

	private Functions() {
		// prevents instantiation
	}

}
