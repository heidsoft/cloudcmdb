package org.cmdbuild.auth;

import org.cmdbuild.auth.user.OperationUser;

public class ThreadLocalUserStore implements UserStore {

	private static final ThreadLocal<OperationUser> threadLocal = new ThreadLocal<>();

	@Override
	public OperationUser getUser() {
		return threadLocal.get();
	}

	@Override
	public void setUser(final OperationUser user) {
		threadLocal.set(user);
	}

}
