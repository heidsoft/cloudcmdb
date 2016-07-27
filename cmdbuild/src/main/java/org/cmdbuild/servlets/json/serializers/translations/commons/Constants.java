package org.cmdbuild.servlets.json.serializers.translations.commons;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.filter.FilterLogic.Filter;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.model.view.View;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.servlets.json.schema.TranslatableElement;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class Constants {

	public static final String IDENTIFIER = "identifier";
	public static final String DESCRIPTION = "description";
	public static final String DEFAULT = "default";
	public static final String TYPE = "type";
	public static final String OWNER = "owner";
	public static final char KEY_SEPARATOR = '.';
	public static final String NO_OWNER = EMPTY;

	public static final List<String> commonHeaders = Lists.newArrayList(IDENTIFIER, DESCRIPTION, DEFAULT);

	public static final AttributesQuery NO_LIMIT_AND_OFFSET = new AttributesQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	public static <T> Iterable<T> nullableIterable(final Iterable<T> it) {
		return it != null ? it : Collections.<T> emptySet();
	}

	public static Predicate<Filter> matchFilterByName(final String name) {
		return new Predicate<Filter>() {
			@Override
			public boolean apply(final Filter input) {
				return name.equals(input.getName());
			}
		};
	}

	public static Predicate<View> matchViewByName(final String name) {
		return new Predicate<View>() {
			@Override
			public boolean apply(final View input) {
				return name.equals(input.getName());
			}
		};
	}

	public static Predicate<Report> matchReportByCode(final String name) {
		return new Predicate<Report>() {
			@Override
			public boolean apply(final Report input) {
				return name.equals(input.getCode());
			}
		};
	}

	public static Converter createConverter(final String type, final String field) {
		final TranslatableElement element = TranslatableElement.of(type);
		final Converter converter = element.createConverter(field);
		Validate.isTrue(converter.isValid());
		return converter;
	}

	private Constants() {
		// prevents instantiation
	}

}
