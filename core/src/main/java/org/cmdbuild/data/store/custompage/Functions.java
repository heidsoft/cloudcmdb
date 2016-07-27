package org.cmdbuild.data.store.custompage;

import com.google.common.base.Function;

public class Functions {

	private static final Function<? super DBCustomPage, String> NAME = new Function<DBCustomPage, String>() {

		@Override
		public String apply(final DBCustomPage input) {
			return input.getName();
		}

	};

	public static final Function<? super DBCustomPage, String> name() {
		return NAME;
	}

	private Functions() {
		// prevents instantiation
	}

}
