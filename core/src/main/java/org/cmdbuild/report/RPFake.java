package org.cmdbuild.report;

import net.sf.jasperreports.engine.design.JRDesignParameter;

public class RPFake extends ReportParameter {

	public RPFake(final String name) {
		super(new JRDesignParameter() {
			{
				setName(name);
				setDescription(name);
			}
		}, name);
	}

	@Override
	public void accept(final ReportParameterVisitor visitor) {
		visitor.accept(this);
	}

	@Override
	public boolean isRequired() {
		return false;
	}

}