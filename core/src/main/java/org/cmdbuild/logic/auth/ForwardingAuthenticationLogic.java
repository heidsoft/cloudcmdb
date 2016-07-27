package org.cmdbuild.logic.auth;

import java.util.List;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingAuthenticationLogic extends ForwardingObject implements AuthenticationLogic {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingAuthenticationLogic() {
	}

	@Override
	protected abstract AuthenticationLogic delegate();

	@Override
	public Response login(final LoginDTO loginDTO, final UserStore userStore) {
		return delegate().login(loginDTO, userStore);
	}

	@Override
	public ClientAuthenticationResponse login(final ClientRequest request, final UserStore userStore) {
		return delegate().login(request, userStore);
	}

	@Override
	public GroupInfo getGroupInfoForGroup(final String groupName) {
		return delegate().getGroupInfoForGroup(groupName);
	}

	@Override
	public List<CMUser> getUsersForGroupWithId(final Long groupId) {
		return delegate().getUsersForGroupWithId(groupId);
	}

	@Override
	public List<Long> getUserIdsForGroupWithId(final Long groupId) {
		return delegate().getUserIdsForGroupWithId(groupId);
	}

	@Override
	public Iterable<String> getGroupNamesForUserWithId(final Long userId) {
		return delegate().getGroupNamesForUserWithId(userId);
	}

	@Override
	public Iterable<String> getGroupNamesForUserWithUsername(final String loginString) {
		return delegate().getGroupNamesForUserWithUsername(loginString);
	}

	@Override
	public CMUser getUserWithId(final Long userId) {
		return delegate().getUserWithId(userId);
	}

	@Override
	public CMUser createUser(final UserDTO userDTO) {
		return delegate().createUser(userDTO);
	}

	@Override
	public CMUser updateUser(final UserDTO userDTO) {
		return delegate().updateUser(userDTO);
	}

	@Override
	public CMGroup getGroupWithId(final Long groupId) {
		return delegate().getGroupWithId(groupId);
	}

	@Override
	public CMGroup getGroupWithName(final String groupName) {
		return delegate().getGroupWithName(groupName);
	}

	@Override
	public CMGroup changeGroupStatusTo(final Long groupId, final boolean isActive) {
		return delegate().changeGroupStatusTo(groupId, isActive);
	}

	@Override
	public Iterable<CMGroup> getAllGroups() {
		return delegate().getAllGroups();
	}

	@Override
	public Iterable<CMUser> getAllUsers(final boolean activeOnly) {
		return delegate().getAllUsers(activeOnly);
	}

	@Override
	public Iterable<CMUser> getServiceOrPrivilegedUsers() {
		return delegate().getServiceOrPrivilegedUsers();
	}

	@Override
	public CMUser enableUserWithId(final Long userId) {
		return delegate().enableUserWithId(userId);
	}

	@Override
	public CMUser disableUserWithId(final Long userId) {
		return delegate().disableUserWithId(userId);
	}

}
