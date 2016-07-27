package org.cmdbuild.logic.auth;

public class RestSessionLogic extends ForwardingSessionLogic {

	private final SessionLogic delegate;

	public RestSessionLogic(final SessionLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected SessionLogic delegate() {
		return delegate;
	}

}
