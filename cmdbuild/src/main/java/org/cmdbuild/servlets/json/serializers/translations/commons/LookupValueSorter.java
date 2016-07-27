package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.ASC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.DESC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.safeInteger;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.safeString;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.data.store.lookup.Lookup;

import com.google.common.collect.Ordering;

public enum LookupValueSorter {

	CODE("code") {
		@Override
		protected Ordering<Lookup> getOrderingForProperty() {
			return ORDER_LOOKUPVALUE_BY_CODE;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<Lookup> getOrderingForProperty() {
			return ORDER_LOOKUPVALUE_BY_DESCRIPTION;
		}
	},
	NUMBER("number") {
		@Override
		protected Ordering<Lookup> getOrderingForProperty() {
			return ORDER_LOOKUPVALUE_BY_NUMBER;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<Lookup> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private LookupValueSorter(final String sorter) {
		this.sorter = sorter;
	}

	public LookupValueSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	abstract Ordering<Lookup> getOrderingForProperty();

	public Ordering<Lookup> getOrientedOrdering() {
		direction = defaultIfBlank(direction, ASC);
		if (direction.equalsIgnoreCase(DESC)) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	public static LookupValueSorter of(final String field) {
		for (final LookupValueSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	private static final Ordering<Lookup> ORDER_LOOKUPVALUE_BY_DESCRIPTION = new Ordering<Lookup>() {
		@Override
		public int compare(final Lookup left, final Lookup right) {
			return safeString(left.description()).compareTo(safeString(right.description()));
		}
	};

	private static final Ordering<Lookup> ORDER_LOOKUPVALUE_BY_NUMBER = new Ordering<Lookup>() {
		@Override
		public int compare(final Lookup left, final Lookup right) {
			return safeInteger(left.number()).compareTo(safeInteger(right.number()));
		}
	};

	private static final Ordering<Lookup> ORDER_LOOKUPVALUE_BY_CODE = new Ordering<Lookup>() {
		@Override
		public int compare(final Lookup left, final Lookup right) {
			return safeString(left.code()).compareTo(safeString(right.code()));
		}
	};

	private static final Ordering<Lookup> DEFAULT_ORDER = ORDER_LOOKUPVALUE_BY_NUMBER;

}
