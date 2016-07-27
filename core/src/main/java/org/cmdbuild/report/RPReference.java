package org.cmdbuild.report;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;

public class RPReference extends ReportParameter {

	private final String className;

	protected RPReference(final JRParameter jrParameter, final String name, final String className) {
		super(jrParameter, name);
		this.className = className;
		if (getJrParameter() == null || getFullName() == null || getFullName().equals("")) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_FORMAT.createException();
		}
		if (getJrParameter().getValueClass() != Integer.class) {
			throw ReportExceptionType.REPORT_INVALID_PARAMETER_REFERENCE_CLASS.createException();
		}
	}

	@Override
	public void accept(final ReportParameterVisitor visitor) {
		visitor.accept(this);
	}

	public String getClassName() {
		return className;
	}

	@Override
	public void parseValue(final Object value) {
		setValue(new IntegerAttributeType().convertValue(value));
	}

}
