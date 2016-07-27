package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.Owner;

import com.google.common.base.Supplier;

public class SessionSupplier implements Supplier<Owner> {

	private final SessionLogic delegate;

	public SessionSupplier(final SessionLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	public Owner get() {
		return new Owner() {

			private final String sessionId = delegate.getCurrent();
			private final String username = delegate.getUser(sessionId).getAuthenticatedUser().getUsername();

			@Override
			public String getId() {
				return sessionId;
			}

			@Override
			public String getDescription() {
				return username;
			}

		};

	}

}
