package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;

import com.google.common.collect.Ordering;

public enum AttributeSorter {

	NAME("name") {
		@Override
		protected Ordering<CMAttribute> getOrderingForProperty() {
			return ORDER_ATTRIBUTE_BY_NAME;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<CMAttribute> getOrderingForProperty() {
			return ORDER_ATTRIBUTE_BY_DESCRIPTION;
		}
	},
	INDEX("index") {
		@Override
		protected Ordering<CMAttribute> getOrderingForProperty() {
			return ORDER_ATTRIBUTE_BY_INDEX;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<CMAttribute> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private AttributeSorter(final String sorter) {
		this.sorter = sorter;
	}

	static final String DESC = "DESC";
	static final String ASC = "ASC";

	abstract Ordering<CMAttribute> getOrderingForProperty();

	public AttributeSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	public Ordering<CMAttribute> getOrientedOrdering() {
		direction = defaultIfBlank(direction, ASC);
		if (direction.equalsIgnoreCase(DESC)) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	public static AttributeSorter of(final String field) {
		for (final AttributeSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_NAME = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return safeString(left.getName()).compareTo(safeString(right.getName()));
		}
	};

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_DESCRIPTION = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return safeString(left.getDescription()).compareTo(safeString(right.getDescription()));
		}
	};

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_INDEX = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return safeInteger(left.getIndex()) > safeInteger(right.getIndex()) ? +1
					: safeInteger(left.getIndex()) < safeInteger(right.getIndex()) ? -1 : 0;
		}
	};

	private static final Ordering<CMAttribute> DEFAULT_ORDER = ORDER_ATTRIBUTE_BY_INDEX;

	static String safeString(final String input) {
		return defaultIfBlank(input, EMPTY);
	}

	static Integer safeInteger(final Integer input) {
		return defaultIfNull(input, 0);
	}

}
