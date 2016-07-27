package org.cmdbuild.logic.auth;

import static com.google.common.collect.Iterables.getFirst;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.cmdbuild.common.Constants.ROLE_CLASS_NAME;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.AuthenticationService.ClientAuthenticatorResponse;
import org.cmdbuild.auth.AuthenticationService.PasswordCallback;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.auth.UserDTO.UserDTOCreationValidator;
import org.cmdbuild.logic.auth.UserDTO.UserDTOUpdateValidator;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

public class DefaultAuthenticationLogic implements AuthenticationLogic {

	private static class DefaultGroupInfo implements GroupInfo {

		private final Long id;
		private final String name;
		private final String description;

		public DefaultGroupInfo(final Long id, final String name, final String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Long getId() {
			return id;
		}

	}

	private static class DefaultResponse implements Response {

		private boolean success = false;
		private String reason = null;
		private Collection<GroupInfo> groups = null;

		private DefaultResponse(final boolean success, final String reason, final Collection<GroupInfo> groups) {
			this.success = success;
			this.reason = reason;
			this.groups = groups;
		}

		public static Response newInstance(final boolean success, final String reason,
				final Collection<GroupInfo> groups) {
			return new DefaultResponse(success, reason, groups);
		}

		@Override
		public boolean isSuccess() {
			return success;
		}

		@Override
		public String getReason() {
			return reason;
		}

		@Override
		public Collection<GroupInfo> getGroupsInfo() {
			return groups;
		}

	}

	private final AuthenticationService authService;
	private final PrivilegeContextFactory privilegeContextFactory;
	private final CMDataView view;
	private final UserStore userStore;

	public DefaultAuthenticationLogic( //
			final AuthenticationService authenticationService, //
			final PrivilegeContextFactory privilegeContextFactory, //
			final CMDataView dataView, //
			final UserStore userStore //
	) {
		this.authService = authenticationService;
		this.privilegeContextFactory = privilegeContextFactory;
		this.view = dataView;
		this.userStore = userStore;
	}

	@Override
	public Response login(final LoginDTO loginDTO, final UserStore userStore) {
		logger.info("trying to login user {} with group {}", loginDTO.getLoginString(), loginDTO.getLoginGroupName());
		logger.trace("login information '{}'", loginDTO);
		final AuthenticatedUser authUser;
		final OperationUser actualOperationUser = userStore.getUser();
		if (actualOperationUser.isValid()) {
			authUser = actualOperationUser.getAuthenticatedUser();
		} else if (!actualOperationUser.getAuthenticatedUser().isAnonymous() && !actualOperationUser.isValid()) {
			/*
			 * header authentication in progress, only group selection is
			 * missing
			 */
			authUser = actualOperationUser.getAuthenticatedUser();
		} else if (loginDTO.isPasswordRequired()) {
			final Login login = Login.newInstance(loginDTO.getLoginString());
			final AuthenticatedUser authenticated = authService.authenticate(login, loginDTO.getPassword());
			authUser = (!loginDTO.isServiceUsersAllowed()
					&& (authenticated.isService() || authenticated.isPrivileged())) ? ANONYMOUS_USER : authenticated;
		} else {
			final Login login = Login.newInstance(loginDTO.getLoginString());
			authUser = authService.authenticate(login, new PasswordCallback() {
				@Override
				public void setPassword(final String password) {
					// nothing to do
				}
			});
		}

		final boolean userNotAuthenticated = authUser.isAnonymous();
		if (userNotAuthenticated) {
			logger.error("Login failed");
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}

		final String groupName = loginDTO.getLoginGroupName();
		PrivilegeContext privilegeCtx = null;
		if (isBlank(groupName)) {
			final CMGroup guessedGroup = guessPreferredGroup(authUser);
			if (guessedGroup == null) {
				logger.error("The user does not have a default group and belongs to multiple groups");
				final List<GroupInfo> groupsForLogin = Lists.newArrayList();
				for (final String name : authUser.getGroupNames()) {
					groupsForLogin.add(getGroupInfoForGroup(name));
				}
				final OperationUser operationUser = new OperationUser(authUser, new NullPrivilegeContext(),
						new NullGroup());
				userStore.setUser(operationUser);
				return DefaultResponse.newInstance(false, AuthExceptionType.AUTH_MULTIPLE_GROUPS.toString(),
						groupsForLogin);
			} else if (authUser.getGroupNames().size() == 1) {
				privilegeCtx = buildPrivilegeContext(guessedGroup);
			} else { // the user has a default group
				final Collection<String> groupNames = authUser.getGroupNames();
				final CMGroup[] groupsArray = new CMGroup[groupNames.size()];
				int i = 0;
				for (final String name : groupNames) {
					groupsArray[i] = getGroupWithName(name);
					i++;
				}
				privilegeCtx = buildPrivilegeContext(groupsArray);
			}
			final OperationUser operationUser = new OperationUser(authUser, privilegeCtx, guessedGroup);
			userStore.setUser(operationUser);
			return buildSuccessfulResponse();
		} else {
			final String selectedGroupName;
			if (authUser.getGroupNames().contains(groupName)) {
				selectedGroupName = groupName;
			} else {
				final String defaultGroupName = authUser.getDefaultGroupName();
				selectedGroupName = (defaultGroupName == null) ? getFirst(authUser.getGroupNames(), groupName)
						: defaultGroupName;
			}
			final CMGroup selectedGroup = getGroupWithName(selectedGroupName);
			privilegeCtx = buildPrivilegeContext(selectedGroup);
			final OperationUser operationUser = new OperationUser(authUser, privilegeCtx, selectedGroup);
			userStore.setUser(operationUser);
			return buildSuccessfulResponse();
		}
	}

	@Override
	public ClientAuthenticationResponse login(final ClientRequest request, final UserStore userStore) {
		logger.info("trying to login with no username or password");
		final ClientAuthenticatorResponse response = authService.authenticate(request);
		final AuthenticatedUser authenticatedUser = response.getUser();
		final boolean isValidUser = !authenticatedUser.isAnonymous();
		final boolean hasOneGroupOnly = (authenticatedUser.getGroupNames().size() == 1);
		final boolean hasDefaultGroup = (authenticatedUser.getDefaultGroupName() != null);
		logger.debug("user is valid: {}", isValidUser);
		logger.debug("user has one group only: {}", hasOneGroupOnly);
		logger.debug("user default group: {}", hasDefaultGroup);
		if (isValidUser) {
			final CMGroup group;
			final PrivilegeContext privilegeContext;
			if (hasOneGroupOnly) {
				final String name = authenticatedUser.getGroupNames().iterator().next();
				group = getGroupWithName(name);
				privilegeContext = buildPrivilegeContext(group);
			} else if (hasDefaultGroup) {
				group = getGroupWithName(authenticatedUser.getDefaultGroupName());
				final CMGroup[] groups = authenticatedUser.getGroupNames().stream() //
						.map(input -> getGroupWithName(input)) //
						.toArray(CMGroup[]::new);
				privilegeContext = buildPrivilegeContext(groups);
			} else {
				group = new NullGroup();
				privilegeContext = new NullPrivilegeContext();
			}
			final OperationUser operationUser = new OperationUser(authenticatedUser, privilegeContext, group);
			userStore.setUser(operationUser);
		}
		return new ClientAuthenticationResponse() {

			@Override
			public String getRedirectUrl() {
				return isValidUser ? null : response.getRedirectUrl();
			}

		};
	}

	/**
	 * Gets the default group (if any) or the only one. If no default group has
	 * been found and more than one group is present, {@code null} is returned.
	 */
	private CMGroup guessPreferredGroup(final CMUser user) {
		String guessedGroupName = user.getDefaultGroupName();
		if (guessedGroupName == null) {
			guessedGroupName = getTheFirstAndOnlyGroupName(user);
		}

		if (guessedGroupName != null) {
			return getGroupWithName(guessedGroupName);
		}
		return null;
	}

	private String getTheFirstAndOnlyGroupName(final CMUser user) {
		String firstGroupName = null;
		final Iterator<String> groupNames = user.getGroupNames().iterator();
		if (groupNames.hasNext()) {
			firstGroupName = groupNames.next();
			if (groupNames.hasNext()) {
				firstGroupName = null;
			}
		}
		return firstGroupName;
	}

	private PrivilegeContext buildPrivilegeContext(final CMGroup... groups) {
		return privilegeContextFactory.buildPrivilegeContext(groups);
	}

	private Response buildSuccessfulResponse() {
		return DefaultResponse.newInstance(true, null, null);
	}

	@Override
	public DefaultGroupInfo getGroupInfoForGroup(final String groupName) {
		final CMClass roleClass = view.findClass(ROLE_CLASS_NAME);
		final CMQueryRow row = view.select(attribute(roleClass, "Description")) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Code"), eq(groupName))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();
		final String description = (String) row.getCard(roleClass).get("Description");
		final Long roleId = row.getCard(roleClass).getId();
		final DefaultGroupInfo groupInfo = new DefaultGroupInfo(roleId, groupName, description);
		return groupInfo;
	}

	@Override
	public List<CMUser> getUsersForGroupWithId(final Long groupId) {
		return authService.fetchUsersByGroupId(groupId);
	}

	@Override
	public List<Long> getUserIdsForGroupWithId(final Long groupId) {
		return authService.fetchUserIdsByGroupId(groupId);
	}

	@Override
	public Iterable<String> getGroupNamesForUserWithId(final Long userId) {
		final CMUser user = authService.fetchUserById(userId);
		return user.getGroupNames();
	}

	@Override
	public Iterable<String> getGroupNamesForUserWithUsername(final String loginString) {
		final CMUser user = authService.fetchUserByUsername(loginString);
		if (user != null) {
			return user.getGroupNames();
		}
		return Lists.newArrayList();
	}

	@Override
	public CMUser getUserWithId(final Long userId) {
		return authService.fetchUserById(userId);
	}

	@Override
	@Transactional
	public CMUser createUser(final UserDTO userDTO) {
		final ModelValidator<UserDTO> validator = new UserDTOCreationValidator();
		if (!validator.validate(userDTO)) {
			throw ORMExceptionType.ORM_CANT_CREATE_USER.createException();
		}
		if (!existsUserWithUsername(userDTO.getUsername())) {
			return authService.createUser(userDTO);
		} else {
			throw ORMExceptionType.ORM_DUPLICATE_USER.createException();
		}
	}

	private boolean existsUserWithUsername(final String username) {
		final CMUser user = authService.fetchUserByUsername(username);
		return user != null;
	}

	@Override
	@Transactional
	public CMUser updateUser(final UserDTO userDTO) {
		final ModelValidator<UserDTO> validator = new UserDTOUpdateValidator();
		if (!validator.validate(userDTO)) {
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		}
		if (userStore.getUser().getPreferredGroup().isRestrictedAdmin() && isNotBlank(userDTO.getPassword())) {
			final CMUser user = authService.fetchUserByUsername(userDTO.getUsername());
			for (final String element : user.getGroupNames()) {
				if (authService.fetchGroupWithName(element).isAdmin()) {
					throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
				}
			}
		}
		final CMUser updatedUser = authService.updateUser(userDTO);
		return updatedUser;
	}

	@Override
	public CMGroup getGroupWithId(final Long groupId) {
		return authService.fetchGroupWithId(groupId);
	}

	@Override
	public CMGroup getGroupWithName(final String groupName) {
		return authService.fetchGroupWithName(groupName);
	}

	@Override
	public CMGroup changeGroupStatusTo(final Long groupId, final boolean isActive) {
		return authService.changeGroupStatusTo(groupId, isActive);
	}

	@Override
	public Iterable<CMGroup> getAllGroups() {
		return authService.fetchAllGroups();
	}

	@Override
	public Iterable<CMUser> getAllUsers(final boolean activeOnly) {
		return authService.fetchAllUsers(activeOnly);
	}

	@Override
	public Iterable<CMUser> getServiceOrPrivilegedUsers() {
		return authService.fetchServiceOrPrivilegedUsers();
	}

	@Override
	public CMUser enableUserWithId(final Long userId) {
		return authService.enableUserWithId(userId);
	}

	@Override
	public CMUser disableUserWithId(final Long userId) {
		return authService.disableUserWithId(userId);
	}

}
