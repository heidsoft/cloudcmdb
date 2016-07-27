package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.user.OperationUser;

public abstract class ForwardingSessionLogic extends ForwardingAuthenticationLogic implements SessionLogic {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingSessionLogic() {
	}

	@Override
	protected abstract SessionLogic delegate();

	@Override
	public String create(final LoginDTO login) {
		return delegate().create(login);
	}

	@Override
	public ClientAuthenticationResponse create(final ClientRequest request, final Callback callback) {
		return delegate().create(request, callback);
	}

	@Override
	public boolean exists(final String value) {
		return delegate().exists(value);
	}

	@Override
	public void update(final String id, final LoginDTO login) {
		delegate().update(id, login);
	}

	@Override
	public void delete(final String id) {
		delegate().delete(id);
	}

	@Override
	public void impersonate(final String id, final String username) {
		delegate().impersonate(id, username);
	}
	
	@Override
	public String getCurrent(){
		return delegate().getCurrent();
	}

	@Override
	public void setCurrent(final String id) {
		delegate().setCurrent(id);
	}

	@Override
	public boolean isValidUser(final String id) {
		return delegate().isValidUser(id);
	}

	@Override
	public OperationUser getUser(final String value) {
		return delegate().getUser(value);
	}

}
