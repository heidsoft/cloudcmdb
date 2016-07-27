package org.cmdbuild.auth;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;

public class UserStores {

	private static class InMemoryUserStore implements UserStore {

		private OperationUser operationUser;

		public InMemoryUserStore() {
			this(null);
		}

		public InMemoryUserStore(final OperationUser preset) {
			this.operationUser = preset;
		}

		@Override
		public OperationUser getUser() {
			return operationUser;
		}

		@Override
		public void setUser(final OperationUser value) {
			operationUser = value;
		}

	}

	public static UserStore inMemory() {
		return new InMemoryUserStore();
	}

	public static UserStore inMemory(final OperationUser preset) {
		return new InMemoryUserStore(preset);
	}

	public static UserStore unsupported() {
		return UnsupportedProxyFactory.of(UserStore.class).create();
	}

	private UserStores() {
		// prevents instantiation
	}

}
