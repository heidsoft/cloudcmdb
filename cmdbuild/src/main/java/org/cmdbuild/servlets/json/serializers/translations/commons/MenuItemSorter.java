package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.ASC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.DESC;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.services.store.menu.MenuItem;

import com.google.common.collect.Ordering;

public enum MenuItemSorter {

	NUMBER("number") {
		@Override
		protected Ordering<MenuItem> getOrderingForProperty() {
			return ORDER_BY_INDEX;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<MenuItem> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private MenuItemSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<MenuItem> getOrderingForProperty();

	public MenuItemSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	public Ordering<MenuItem> getOrientedOrdering() {
		direction = defaultIfBlank(direction, ASC);
		if (direction.equalsIgnoreCase(DESC)) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	static MenuItemSorter of(final String field) {
		for (final MenuItemSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	private static final Ordering<MenuItem> ORDER_BY_INDEX = new Ordering<MenuItem>() {
		@Override
		public int compare(final MenuItem left, final MenuItem right) {
			return left.getIndex() > right.getIndex() ? +1 : left.getIndex() < right.getIndex() ? -1 : 0;
		}
	};

	private static final Ordering<MenuItem> DEFAULT_ORDER = ORDER_BY_INDEX;
}
