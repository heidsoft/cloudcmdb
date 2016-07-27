package org.cmdbuild.auth;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.logic.auth.GroupDTO;
import org.cmdbuild.logic.auth.UserDTO;

public interface AuthenticationService {

	interface PasswordCallback {

		void setPassword(String password);
	}

	class ClientAuthenticatorResponse {

		private final AuthenticatedUser user;
		private final String redirectUrl;

		public static final ClientAuthenticatorResponse EMTPY_RESPONSE = new ClientAuthenticatorResponse(ANONYMOUS_USER,
				null);

		public ClientAuthenticatorResponse(final AuthenticatedUser user, final String redirectUrl) {
			Validate.notNull(user);
			this.user = user;
			this.redirectUrl = redirectUrl;
		}

		public final AuthenticatedUser getUser() {
			return user;
		}

		public final String getRedirectUrl() {
			return redirectUrl;
		}
	}

	void setPasswordAuthenticators(final PasswordAuthenticator... passwordAuthenticators);

	void setClientRequestAuthenticators(final ClientRequestAuthenticator... clientRequestAuthenticators);

	void setUserFetchers(final UserFetcher... userFetchers);

	void setGroupFetcher(final GroupFetcher groupFetcher);

	/**
	 * Actively checks the user credentials and returns the authenticated user
	 * on success.
	 * 
	 * @param login
	 * @param password
	 *            unencrypted password
	 * @return the user that was authenticated
	 */
	AuthenticatedUser authenticate(final Login login, final String password);

	/**
	 * Extracts the unencrypted password for the user and sets it in the
	 * 
	 * @param passwordCallback
	 *            for further processing.
	 * 
	 * @param login
	 * @param passwordCallback
	 *            object where to set the unencrypted password
	 * @return the user to be authenticated as if the authentication succeeded
	 */
	AuthenticatedUser authenticate(final Login login, final PasswordCallback passwordCallback);

	/**
	 * Tries to authenticate the user with a ClientRequestAuthenticator
	 * 
	 * @param request
	 *            object representing a client request
	 * @return response object with the authenticated user or a redirect URL
	 */
	ClientAuthenticatorResponse authenticate(final ClientRequest request);

	List<CMUser> fetchUsersByGroupId(Long groupId);

	List<Long> fetchUserIdsByGroupId(Long groupId);

	/**
	 * Given a user identifier, it returns the user with that id
	 * 
	 * @param userId
	 * @return the user with id = userId, null if there is no user with that id
	 */
	CMUser fetchUserById(Long userId);

	/**
	 * Given a username, it returns the user with that username
	 * 
	 * @param username
	 * @return the user with the provided username, null if there is no user
	 *         with that username
	 */
	CMUser fetchUserByUsername(String username);

	/**
	 * Creates a new user in the database
	 * 
	 * @param userDTO
	 *            a DTO that contains some details about new user (username,
	 *            password, active flag, email ...)
	 * @return
	 */
	CMUser createUser(UserDTO userDTO);

	/**
	 * Updates an existent user in the database
	 * 
	 * @param userDTO
	 *            a DTO that contains some details about the user that will be
	 *            updated (username, password, active flag, email ...)
	 * @return
	 */
	CMUser updateUser(UserDTO userDTO);

	/**
	 * Creates a new group in the database
	 * 
	 * @param groupDTO
	 *            a DTO that contains some details about new user (name, active
	 *            flag, email ...)
	 * @return
	 */
	CMGroup createGroup(GroupDTO groupDTO);

	/**
	 * Updates an existent group in the database
	 * 
	 * @param groupDTO
	 *            a DTO that contains some details about the group that will be
	 *            updated (name, active flag, email, groupId ...)
	 * @return
	 */
	CMGroup updateGroup(GroupDTO groupDTO);

	/**
	 * Use it to activate/deactivate an existing group
	 * 
	 * @param active
	 * @return
	 */
	CMGroup setGroupActive(Long groupId, boolean active);

	/**
	 * 
	 * @return a collection of all groups stored in the database
	 */
	Iterable<CMGroup> fetchAllGroups();

	/**
	 * @param activeOnly
	 * 
	 * @return a collection of all users stored in the database
	 */
	Iterable<CMUser> fetchAllUsers(boolean activeOnly);

	Iterable<CMUser> fetchServiceOrPrivilegedUsers();

	/**
	 * Retrieves a group with the specified id
	 * 
	 * @param groupId
	 *            the id of the group that will be retrieved
	 * @return
	 */
	CMGroup fetchGroupWithId(Long groupId);

	/**
	 * Retrieves a group with the specified name
	 * 
	 * @param groupName
	 *            the name of the group that will be retrieved
	 * @return
	 */
	CMGroup fetchGroupWithName(String groupName);

	/**
	 * Enable the user with the current user id. If already enabled it does
	 * nothing
	 * 
	 * @param userId
	 * @return
	 */
	CMUser enableUserWithId(Long userId);

	/**
	 * Disable the user with the current user id. If already disabled it does
	 * nothing
	 * 
	 * @param userId
	 * @return
	 */
	CMUser disableUserWithId(Long userId);

	/**
	 * It changes the status of the role with id = groupId
	 * 
	 * @param groupId
	 *            the id of the group whose state will be changed
	 * @param isActive
	 * @return
	 */
	CMGroup changeGroupStatusTo(Long groupId, boolean isActive);

}
