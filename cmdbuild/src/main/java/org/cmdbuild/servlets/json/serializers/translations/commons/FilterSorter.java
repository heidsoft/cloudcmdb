package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.ASC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.DESC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.safeString;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.services.store.filter.FilterStore;

import com.google.common.collect.Ordering;

public enum FilterSorter {

	NAME("name") {
		@Override
		protected Ordering<FilterStore.Filter> getOrderingForProperty() {
			return ORDER_FILTER_BY_NAME;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<FilterStore.Filter> getOrderingForProperty() {
			return ORDER_FILTER_BY_DESCRIPTION;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<FilterStore.Filter> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private FilterSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<FilterStore.Filter> getOrderingForProperty();

	public FilterSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	public Ordering<FilterStore.Filter> getOrientedOrdering() {
		direction = defaultIfBlank(direction, ASC);
		if (direction.equalsIgnoreCase(DESC)) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	static FilterSorter of(final String field) {
		for (final FilterSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	private static final Ordering<FilterStore.Filter> ORDER_FILTER_BY_NAME = new Ordering<FilterStore.Filter>() {
		@Override
		public int compare(final FilterStore.Filter left, final FilterStore.Filter right) {
			return safeString(left.getName()) //
					.compareTo(safeString(right.getName()));
		}
	};

	private static final Ordering<FilterStore.Filter> ORDER_FILTER_BY_DESCRIPTION = new Ordering<FilterStore.Filter>() {
		@Override
		public int compare(final FilterStore.Filter left, final FilterStore.Filter right) {
			return safeString(left.getDescription()) //
					.compareTo(safeString(right.getDescription()));
		}
	};

	private static final Ordering<FilterStore.Filter> DEFAULT_ORDER = ORDER_FILTER_BY_DESCRIPTION;
}
