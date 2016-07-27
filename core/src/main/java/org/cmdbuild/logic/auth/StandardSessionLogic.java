package org.cmdbuild.logic.auth;

public class StandardSessionLogic extends ForwardingSessionLogic {

	private final SessionLogic delegate;

	public StandardSessionLogic(final SessionLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected SessionLogic delegate() {
		return delegate;
	}

}
