package org.cmdbuild.auth;

import org.cmdbuild.auth.user.OperationUser;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingUserStore extends ForwardingObject implements UserStore {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingUserStore() {
	}

	@Override
	protected abstract UserStore delegate();

	@Override
	public OperationUser getUser() {
		return delegate().getUser();
	}

	@Override
	public void setUser(final OperationUser user) {
		delegate().setUser(user);
	}

}
