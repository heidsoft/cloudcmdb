package org.cmdbuild.report;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.joda.time.DateTime;

public class RPSimple extends ReportParameter implements LoggingSupport {

	protected RPSimple(final JRParameter jrParameter, final String name) {
		super(jrParameter, name);
		if (getJrParameter() == null || getFullName() == null || getFullName().equals("")) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}
	}

	@Override
	public void accept(final ReportParameterVisitor visitor) {
		visitor.accept(this);
	}

	@Override
	public void parseValue(final Object value) {
		try {
			final Object output;
			/*
			 * ugly solution due to missing time to find a more decent way for
			 * handle all possible conversions
			 */
			final String newValue = value.toString();
			if (isNotBlank(newValue)) {
				if (getJrParameter().getValueClass() == String.class) {
					output = newValue;
				} else if (getJrParameter().getValueClass() == Integer.class
						|| getJrParameter().getValueClass() == Number.class) {
					output = Integer.parseInt(newValue);
				} else if (getJrParameter().getValueClass() == Long.class) {
					output = Long.parseLong(newValue);
				} else if (getJrParameter().getValueClass() == Short.class) {
					output = Short.parseShort(newValue);
				} else if (getJrParameter().getValueClass() == BigDecimal.class) {
					output = new BigDecimal(Integer.parseInt(newValue));
				} else if (getJrParameter().getValueClass() == Date.class) {
					output = parseDate(newValue);
				} else if (getJrParameter().getValueClass() == Timestamp.class) {
					output = new Timestamp(parseDate(newValue).getTime());
				} else if (getJrParameter().getValueClass() == Time.class) {
					output = new Time(parseDate(newValue).getTime());
				} else if (getJrParameter().getValueClass() == Double.class) {
					output = Double.parseDouble(newValue);
				} else if (getJrParameter().getValueClass() == Float.class) {
					output = Float.parseFloat(newValue);
				} else if (getJrParameter().getValueClass() == Boolean.class) {
					output = Boolean.parseBoolean(newValue);
				} else {
					throw ReportExceptionType.REPORT_INVALID_PARAMETER_CLASS.createException();
				}
			} else {
				output = null;
			}
			setValue(output);
		} catch (final Exception e) {
			logger.error("Invalid parameter value \"" + value + "\" for \"" + getJrParameter().getValueClass() + "\"",
					e);
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_VALUE.createException();
		}
	}

	private Date parseDate(final String newValue) {
		return DateTime.class
				.cast(ReportParameterConverter.of(this) //
						.toCMAttribute() //
						.getType() //
						.convertValue(newValue)) //
				.toDate();
	}

}
