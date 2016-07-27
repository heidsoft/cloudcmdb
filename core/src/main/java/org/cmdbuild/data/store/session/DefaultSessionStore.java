package org.cmdbuild.data.store.session;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.store.ForwardingStore;
import org.cmdbuild.data.store.Store;

public class DefaultSessionStore extends ForwardingStore<Session> implements SessionStore {

	private final Store<Session> delegate;

	public DefaultSessionStore(final Store<Session> delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Store<Session> delegate() {
		return delegate;
	}

	@Override
	public OperationUser selectUserOrImpersonated(final Session session) {
		final OperationUser impersonated = session.getImpersonated();
		return (impersonated != null) ? impersonated : session.getUser();
	}

}
