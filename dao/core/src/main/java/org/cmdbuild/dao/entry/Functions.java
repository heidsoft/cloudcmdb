package org.cmdbuild.dao.entry;

import com.google.common.base.Function;

public class Functions {

	private static class ToAttributeValue<T> implements Function<CMCard, T> {

		private final String name;
		private final Class<T> type;

		public ToAttributeValue(final String name, final Class<T> type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public T apply(final CMCard input) {
			return input.get(name, type);
		}

	}

	public static <T> Function<CMCard, T> toAttributeValue(final String name, final Class<T> type) {
		return new ToAttributeValue<T>(name, type);
	}

	private Functions() {
		// prevents instantiation
	}

}
