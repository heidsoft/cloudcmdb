package org.cmdbuild.dao.entrytype.attributetype;

import java.math.BigDecimal;

import org.apache.commons.lang3.Validate;

public class DecimalAttributeType extends AbstractAttributeType<BigDecimal> {

	public final Integer precision;
	public final Integer scale;

	public DecimalAttributeType() {
		this.precision = null;
		this.scale = null;
	}

	public DecimalAttributeType(final Integer precision, final Integer scale) {
		Validate.isTrue(precision > 0);
		Validate.isTrue(scale >= 0 && precision >= scale);
		this.precision = precision;
		this.scale = scale;
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected BigDecimal convertNotNullValue(final Object value) {
		BigDecimal decimalValue;
		if (value instanceof BigDecimal) {
			decimalValue = BigDecimal.class.cast(value);
		} else if (value instanceof String) {
			final String stringValue = String.class.cast(value);
			if (stringValue.isEmpty()) {
				decimalValue = null;
			} else {
				// Throws NumberFormatException if the number cannot be parsed
				decimalValue = new BigDecimal(stringValue);
			}
		} else if (value instanceof Double) {
			decimalValue = BigDecimal.valueOf(Double.class.cast(value));
		} else if (value instanceof Integer) {
			decimalValue = BigDecimal.valueOf(Integer.class.cast(value).longValue());
		} else {
			throw illegalValue(value);
		}

		return decimalValue;
	}

}
