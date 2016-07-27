package org.cmdbuild.auth;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;

public class AnonymousWhenMissingUserStore extends ForwardingUserStore {

	private final UserStore delegate;

	public AnonymousWhenMissingUserStore(final UserStore delegate) {
		this.delegate = delegate;
	}

	@Override
	protected UserStore delegate() {
		return delegate;
	}

	@Override
	public OperationUser getUser() {
		OperationUser operationUser = super.getUser();
		if (operationUser == null) {
			operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(), new NullGroup());
			setUser(operationUser);
		}
		return operationUser;
	}

}
