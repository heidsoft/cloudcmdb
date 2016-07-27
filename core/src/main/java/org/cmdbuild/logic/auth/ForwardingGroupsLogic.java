package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.acl.CMGroup;

public abstract class ForwardingGroupsLogic implements GroupsLogic {

	private final GroupsLogic delegate;

	protected ForwardingGroupsLogic(final GroupsLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	public CMGroup createGroup(final GroupDTO groupDTO) {
		return delegate.createGroup(groupDTO);
	}

	@Override
	public CMGroup updateGroup(final GroupDTO groupDTO) {
		return delegate.updateGroup(groupDTO);
	}

	@Override
	public CMGroup setGroupActive(final Long groupId, final boolean active) {
		return delegate.setGroupActive(groupId, active);
	}

	@Override
	public void addUserToGroup(final Long userId, final Long groupId) {
		delegate.addUserToGroup(userId, groupId);
	}

	@Override
	public void removeUserFromGroup(final Long userId, final Long groupId) {
		delegate.removeUserFromGroup(userId, groupId);
	}

}
