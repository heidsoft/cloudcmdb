package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.ASC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.DESC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.safeString;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.model.view.View;

import com.google.common.collect.Ordering;

public enum ViewSorter {

	NAME("name") {
		@Override
		protected Ordering<View> getOrderingForProperty() {
			return ORDER_VIEW_BY_NAME;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<View> getOrderingForProperty() {
			return ORDER_VIEW_BY_DESCRIPTION;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<View> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private ViewSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<View> getOrderingForProperty();

	public ViewSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	public Ordering<View> getOrientedOrdering() {
		direction = defaultIfBlank(direction, ASC);
		if (direction.equalsIgnoreCase(DESC)) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	static ViewSorter of(final String field) {
		for (final ViewSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	private static final Ordering<View> ORDER_VIEW_BY_NAME = new Ordering<View>() {
		@Override
		public int compare(final View left, final View right) {
			return safeString(left.getName()).compareTo(safeString(right.getName()));
		}
	};

	private static final Ordering<View> ORDER_VIEW_BY_DESCRIPTION = new Ordering<View>() {
		@Override
		public int compare(final View left, final View right) {
			return safeString(left.getDescription()).compareTo(safeString(right.getDescription()));
		}
	};

	private static final Ordering<View> DEFAULT_ORDER = ORDER_VIEW_BY_DESCRIPTION;
}
