package org.cmdbuild.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;

public class RPLookup extends ReportParameter {

	private final String lookupType;

	protected RPLookup(final JRParameter jrParameter, final String name, final String lookupType) {
		super(jrParameter, name);
		this.lookupType = lookupType;
		if (getJrParameter() == null || getFullName() == null || getFullName().equals("")) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}
		if (getJrParameter().getValueClass() != Integer.class) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_LOOKUP_CLASS.createException();
		}
	}

	@Override
	public void accept(final ReportParameterVisitor visitor) {
		visitor.accept(this);
	}

	public String getLookupName() {
		return lookupType;
	}

	@Override
	public void parseValue(final Object value) {
		setValue(new IntegerAttributeType().convertValue(value));
	}

}
