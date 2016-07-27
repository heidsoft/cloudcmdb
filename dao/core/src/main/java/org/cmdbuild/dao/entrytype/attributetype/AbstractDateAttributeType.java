package org.cmdbuild.dao.entrytype.attributetype;

import static org.cmdbuild.common.Constants.DATE_FOUR_DIGIT_YEAR_FORMAT;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

public abstract class AbstractDateAttributeType extends AbstractAttributeType<DateTime> {

	@Override
	protected DateTime convertNotNullValue(final Object value) {
		if (value instanceof String) {
			return convertDateString((String) value);
		} else if (value instanceof java.util.Date) {
			final long instant = ((java.util.Date) value).getTime();
			return new DateTime(instant);
		} else if (value instanceof DateTime) {
			return (DateTime) value;
		} else if (value instanceof Calendar) {
			final long instant = ((Calendar) value).getTimeInMillis();
			return new DateTime(instant);
		} else {
			throw illegalValue(value);
		}
	}

	protected final DateTime convertDateString(final String stringValue) {

		if (StringUtils.EMPTY.equals(stringValue)) {
			return null;
		}

		for (final DateTimeFormatter formatter : getFormatters()) {
			try {
				return formatter.parseDateTime(stringValue);
			} catch (final IllegalArgumentException e) {
				// try the next one
			}
		}

		// if we don't enable lenient processing, dates between
		// 1916-06-03 and 1920-03-21,
		// 1940-06-15, 1947-03-16, and
		// 1966-05-22 to 1979-05-27 don't work
		// (central european timezone CET)
		return convertNotConveredDate(stringValue);
	}

	abstract protected DateTimeFormatter[] getFormatters();

	public final DateTime convertNotConveredDate(final String stringValue) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FOUR_DIGIT_YEAR_FORMAT);
		dateFormat.setLenient(true);
		try {
			final Date date = dateFormat.parse(stringValue);
			return convertNotNullValue(date);
		} catch (final ParseException e) {
		}
		return null;
	}
}
