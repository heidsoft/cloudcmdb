package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.ASC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.DESC;
import static org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter.safeString;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.services.store.report.Report;

import com.google.common.collect.Ordering;

public enum ReportSorter {

	NAME("name") {
		@Override
		protected Ordering<Report> getOrderingForProperty() {
			return ORDER_REPORT_BY_CODE;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<Report> getOrderingForProperty() {
			return ORDER_REPORT_BY_DESCRIPTION;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<Report> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private ReportSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<Report> getOrderingForProperty();

	public ReportSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	public Ordering<Report> getOrientedOrdering() {
		direction = defaultIfBlank(direction, ASC);
		if (direction.equalsIgnoreCase(DESC)) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	static ReportSorter of(final String field) {
		for (final ReportSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	private static final Ordering<Report> ORDER_REPORT_BY_CODE = new Ordering<Report>() {
		@Override
		public int compare(final Report left, final Report right) {
			return safeString(left.getCode()).compareTo(safeString(right.getCode()));
		}
	};

	private static final Ordering<Report> ORDER_REPORT_BY_DESCRIPTION = new Ordering<Report>() {
		@Override
		public int compare(final Report left, final Report right) {
			return safeString(left.getDescription()).compareTo(safeString(right.getDescription()));
		}
	};

	private static final Ordering<Report> DEFAULT_ORDER = ORDER_REPORT_BY_DESCRIPTION;
}
