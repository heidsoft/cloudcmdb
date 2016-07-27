package org.cmdbuild.workflow.xpdl;

import org.apache.commons.lang3.Validate;

public class CMActivityVariableToProcess {

	private final String name;
	private final boolean writable;
	private final boolean mandatory;

	public CMActivityVariableToProcess(final String name, final boolean writable, final boolean mandatory) {
		Validate.notEmpty(name, "Variable names must be non-empty");
		this.name = name;
		this.writable = writable;
		this.mandatory = mandatory;
	}

	public String getName() {
		return name;
	}

	public boolean isWritable() {
		return writable;
	}

	public boolean isMandatory() {
		return mandatory;
	}

}
