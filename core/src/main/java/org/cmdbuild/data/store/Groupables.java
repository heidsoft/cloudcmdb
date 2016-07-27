package org.cmdbuild.data.store;

public class Groupables {

	private static class NullGroupable implements Groupable {

		@Override
		public String getGroupAttributeName() {
			return null;
		}

		@Override
		public Object getGroupAttributeValue() {
			return null;
		}

	};

	private static final Groupable NOT_GROUPABLE = new NullGroupable();

	private static class NameAndValue implements Groupable {

		private final String name;
		private final Object value;

		public NameAndValue(final String name, final Object value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String getGroupAttributeName() {
			return name;
		}

		@Override
		public Object getGroupAttributeValue() {
			return value;
		}

	};

	public static Groupable notGroupable() {
		return NOT_GROUPABLE;
	}

	public static Groupable nameAndValue(final String name, final Object value) {
		return new NameAndValue(name, value);
	}

	private Groupables() {
		// prevents instantiation
	}

}
