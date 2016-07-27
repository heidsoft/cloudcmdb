package org.cmdbuild.logic.auth;

import org.springframework.transaction.annotation.Transactional;

public class TransactionalGroupsLogic extends ForwardingGroupsLogic {

	public TransactionalGroupsLogic(final GroupsLogic delegate) {
		super(delegate);
	}

	@Transactional
	@Override
	public void addUserToGroup(final Long userId, final Long groupId) {
		super.addUserToGroup(userId, groupId);
	}

	@Transactional
	@Override
	public void removeUserFromGroup(final Long userId, final Long groupId) {
		super.removeUserFromGroup(userId, groupId);
	}

}
