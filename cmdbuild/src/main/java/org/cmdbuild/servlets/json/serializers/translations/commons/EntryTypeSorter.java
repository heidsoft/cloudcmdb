package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.ASC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.DESC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.safeString;

import org.cmdbuild.dao.entrytype.CMEntryType;

import com.google.common.collect.Ordering;

public enum EntryTypeSorter {
	NAME("name") {
		@Override
		protected Ordering<CMEntryType> getOrderingForProperty() {
			return ORDER_ENTRYTYPE_BY_NAME;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<CMEntryType> getOrderingForProperty() {
			return ORDER_ENTRYTYPE_BY_DESCRIPTION;
		}
	},
	DEFAULT(EMPTY) {
		@Override
		protected Ordering<CMEntryType> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private EntryTypeSorter(final String sorter) {
		this.sorter = sorter;
	}

	public EntryTypeSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	abstract Ordering<CMEntryType> getOrderingForProperty();

	public Ordering<CMEntryType> getOrientedOrdering() {
		direction = defaultIfBlank(direction, ASC);
		if (direction.equalsIgnoreCase(DESC)) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	public static EntryTypeSorter of(final String field) {
		for (final EntryTypeSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	private static final Ordering<CMEntryType> ORDER_ENTRYTYPE_BY_NAME = new Ordering<CMEntryType>() {
		@Override
		public int compare(final CMEntryType left, final CMEntryType right) {
			return safeString(left.getName()) //
					.compareTo(safeString(right.getName()));
		}
	};

	private static final Ordering<CMEntryType> ORDER_ENTRYTYPE_BY_DESCRIPTION = new Ordering<CMEntryType>() {
		@Override
		public int compare(final CMEntryType left, final CMEntryType right) {
			return safeString(left.getDescription()) //
					.compareTo(safeString(right.getDescription()));
		}
	};

	private static final Ordering<CMEntryType> DEFAULT_ORDER = ORDER_ENTRYTYPE_BY_DESCRIPTION;

}
