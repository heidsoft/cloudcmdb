package org.cmdbuild.services;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.listeners.ValuesStore;

public class ValuesStoreBasedUserStore implements UserStore {

	private static final String AUTH_KEY = "auth";

	private final ValuesStore valuesStore;

	public ValuesStoreBasedUserStore(final ValuesStore valuesStore) {
		this.valuesStore = valuesStore;
	}

	@Override
	public OperationUser getUser() {
		OperationUser operationUser = (OperationUser) valuesStore.get(AUTH_KEY);
		if (operationUser == null) {
			operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(), new NullGroup());
			setUser(operationUser);
		}
		return operationUser;
	}

	@Override
	public void setUser(final OperationUser user) {
		valuesStore.set(AUTH_KEY, user);
	}

}
