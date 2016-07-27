package org.cmdbuild.workflow.xpdl;

public class XpdlException extends CMProcessDefinitionException {

	private static final long serialVersionUID = -853518722848389606L;

	public XpdlException(final Throwable cause) {
		super(cause);
	}

	public XpdlException(final String message) {
		super(message);
	}
}