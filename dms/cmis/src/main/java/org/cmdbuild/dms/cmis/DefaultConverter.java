package org.cmdbuild.dms.cmis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dms.MetadataType;

public class DefaultConverter implements Converter {

	private static final SimpleDateFormat CMDBUILD_DATETIME_PARSING_FORMAT = new SimpleDateFormat(
			Constants.SOAP_ALL_DATES_PARSING_PATTERN);
	private static final SimpleDateFormat CMDBUILD_DATETIME_PRINTING_FORMAT = new SimpleDateFormat(
			Constants.DATETIME_PRINTING_PATTERN);
	private static final SimpleDateFormat CMDBUILD_DATE_PRINTING_FORMAT = new SimpleDateFormat(
			Constants.DATE_PRINTING_PATTERN);

	@Override
	public void setContext(final Context context) {
	}

	@Override
	public boolean isAsymmetric() {
		return false;
	}

	@Override
	public MetadataType getType(final PropertyDefinition<?> propertyDefinition) {
		if (propertyDefinition.getChoices() != null && !propertyDefinition.getChoices().isEmpty()) {
			return MetadataType.LIST;
		} else {
			final PropertyType type = propertyDefinition.getPropertyType();
			if (type == PropertyType.BOOLEAN) {
				return MetadataType.BOOLEAN;
			} else if (type == PropertyType.INTEGER) {
				return MetadataType.INTEGER;
			} else if (type == PropertyType.DECIMAL) {
				return MetadataType.FLOAT;
			} else if (type == PropertyType.DATETIME) {
				if (((PropertyDateTimeDefinition) propertyDefinition)
						.getDateTimeResolution() == DateTimeResolution.DATE) {
					return MetadataType.DATE;
				} else {
					return MetadataType.DATETIME;
				}
			} else {
				return MetadataType.TEXT;
			}
		}
	}

	@Override
	public Object convertToCmisValue(final Session session, final PropertyDefinition<?> propertyDefinition,
			final String value) {
		try {
			Object cmisValue = null;
			if (value != null && !value.isEmpty()) {
				switch (getType(propertyDefinition)) {
				case INTEGER: {
					cmisValue = Integer.valueOf(value);
					break;
				}
				case FLOAT: {
					cmisValue = Double.valueOf(value);
					break;
				}
				case BOOLEAN: {
					cmisValue = Boolean.valueOf(value);
					break;
				}
				case DATE:
				case DATETIME: {
					final Date date = CMDBUILD_DATETIME_PARSING_FORMAT.parse(value);
					final GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime(date);
					cmisValue = calendar;
					break;
				}
				default: {
					cmisValue = value;
				}
				}
			}
			return cmisValue;
		} catch (final Exception e) {
			return null;
		}
	}

	@Override
	public String convertFromCmisValue(final Session session, final PropertyDefinition<?> propertyDefinition,
			final Object value) {
		try {
			String output = null;
			if (value != null) {
				switch (getType(propertyDefinition)) {
				case DATE: {
					final GregorianCalendar calendar = (GregorianCalendar) value;
					output = CMDBUILD_DATE_PRINTING_FORMAT.format(calendar.getTime());
					break;
				}
				case DATETIME: {
					final GregorianCalendar calendar = (GregorianCalendar) value;
					output = CMDBUILD_DATETIME_PRINTING_FORMAT.format(calendar.getTime());
					break;
				}
				default: {
					output = value != null ? value.toString() : null;
				}
				}
			}
			return output;
		} catch (final Exception e) {
			return "";
		}
	}

}
