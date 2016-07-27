package org.cmdbuild.common.utils;

import org.apache.commons.lang3.builder.Builder;

public class BuilderUtils {

	public static <T> T a(final Builder<T> builder) {
		return builder.build();
	}

	public static <T> T an(final Builder<T> builder) {
		return builder.build();
	}

	public static <T> T build(final Builder<T> builder) {
		return builder.build();
	}

	private BuilderUtils() {
		// prevents instantiation
	}

}
