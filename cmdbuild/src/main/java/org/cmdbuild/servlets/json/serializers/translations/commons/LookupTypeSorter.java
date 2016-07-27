package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.ASC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.DESC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.safeString;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.data.store.lookup.LookupType;

import com.google.common.collect.Ordering;

public enum LookupTypeSorter {

	DESCRIPTION("description") {
		@Override
		protected Ordering<LookupType> getOrderingForProperty() {
			return ORDER_LOOKUPTYPE_BY_DESCRIPTION;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<LookupType> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private LookupTypeSorter(final String sorter) {
		this.sorter = sorter;
	}

	public LookupTypeSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	abstract Ordering<LookupType> getOrderingForProperty();

	public Ordering<LookupType> getOrientedOrdering() {
		direction = defaultIfBlank(direction, ASC);
		if (direction.equalsIgnoreCase(DESC)) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	public static LookupTypeSorter of(final String field) {
		for (final LookupTypeSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	/*
	 * what the client calls 'description' for the server is 'name'
	 */
	private static final Ordering<LookupType> ORDER_LOOKUPTYPE_BY_DESCRIPTION = new Ordering<LookupType>() {
		@Override
		public int compare(final LookupType left, final LookupType right) {
			return safeString(left.name).compareTo(safeString(right.name));
		}
	};

	private static final Ordering<LookupType> DEFAULT_ORDER = ORDER_LOOKUPTYPE_BY_DESCRIPTION;

}
