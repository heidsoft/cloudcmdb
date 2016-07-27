package org.cmdbuild.services.store.menu;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.Comparator;

public class Comparators {

	private static Comparator<MenuItem> BY_INDEX = new Comparator<MenuItem>() {

		@Override
		public int compare(final MenuItem o1, final MenuItem o2) {
			final int index1 = o1.getIndex();
			final int index2 = o2.getIndex();
			if (index1 < index2) {
				return -1;
			} else if (index1 > index2) {
				return 1;
			} else {
				return 0;
			}
		}

	};

	private static Comparator<MenuItem> BY_DESCRIPTION = new Comparator<MenuItem>() {

		@Override
		public int compare(final MenuItem o1, final MenuItem o2) {
			return description(o1).compareTo(description(o2));
		}

		/**
		 * Description can be null on DB, to avoid nullPointerException compare
		 * an empty string if has null value
		 */
		private String description(final MenuItem item) {
			final String description = item.getDescription();
			return defaultString(description);
		}

	};

	public static Comparator<MenuItem> byIndex() {
		return BY_INDEX;
	}

	public static Comparator<MenuItem> byDescription() {
		return BY_DESCRIPTION;
	}

	private Comparators() {
		// prevents instantiation
	}

}
