package org.cmdbuild.logic.auth;

public class SoapSessionLogic extends ForwardingSessionLogic {

	private final SessionLogic delegate;

	public SoapSessionLogic(final SessionLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected SessionLogic delegate() {
		return delegate;
	}

}
