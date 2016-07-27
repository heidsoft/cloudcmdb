package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ALREADY_ASSOCIATED;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_GROUP;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DISABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.EMAIL;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUP_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.IS_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.NEW_PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.OLD_PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGED;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_MODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_OBJ_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_READ;
import static org.cmdbuild.servlets.json.CommunicationConstants.PRIVILEGE_WRITE;
import static org.cmdbuild.servlets.json.CommunicationConstants.RESULT;
import static org.cmdbuild.servlets.json.CommunicationConstants.ROWS;
import static org.cmdbuild.servlets.json.CommunicationConstants.SERVICE;
import static org.cmdbuild.servlets.json.CommunicationConstants.STARTING_CLASS;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.UI_CONFIGURATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.USERS;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER_NAME;

import java.io.IOException;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.ForwardingSerializablePrivilege;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.GroupInfo;
import org.cmdbuild.logic.auth.GroupDTO;
import org.cmdbuild.logic.auth.GroupDTO.GroupDTOBuilder;
import org.cmdbuild.logic.auth.UserDTO;
import org.cmdbuild.logic.auth.UserDTO.UserDTOBuilder;
import org.cmdbuild.logic.privileges.CardEditMode;
import org.cmdbuild.logic.privileges.PrivilegeInfo;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.model.profile.UIConfiguration;
import org.cmdbuild.model.profile.UIConfigurationObjectMapper;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.PrivilegeSerializer;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ModSecurity extends JSONBaseWithSpringContext {

	private static final String REMOVE = "remove";
	private static final String CLONE = "clone";
	private static final String MODIFY = "modify";
	private static final String CREATE = "create";
	public static final String CARD_EDIT_MODE_JSON_FORMAT = "{\"modify\": %b, \"clone\": %b, \"remove\": %b, \"create\": %b}";
	private static final ObjectMapper mapper = new UIConfigurationObjectMapper();

	/*
	 * Group management
	 */

	@JSONExported
	public JSONObject getGroupList() throws JSONException, AuthException, ORMException {
		final Iterable<CMGroup> allGroups = authLogic().getAllGroups();
		final JSONObject out = new JSONObject();
		final JSONArray groups = new JSONArray();

		for (final CMGroup group : allGroups) {
			final JSONObject jsonGroup = Serializer.serialize(group);
			groups.put(jsonGroup);
		}

		out.put(GROUPS, groups);
		return out;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject saveGroup( //
			@Parameter(ID) final Long groupId, //
			@Parameter(value = NAME, required = false) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(EMAIL) final String email, //
			@Parameter(STARTING_CLASS) final Long startingClass, //
			@Parameter(IS_ACTIVE) final boolean isActive, //
			@Parameter(value = TYPE, required = false) final String groupType //
	) throws JSONException, AuthException {
		final boolean newGroup = groupId <= -1;
		CMGroup createdOrUpdatedGroup = null;
		final GroupDTOBuilder builder = GroupDTO.newInstance() //
				.withName(name) //
				.withDescription(description) //
				.withEmail(email) //
				.withStartingClassId(startingClass) //
				.withActiveStatus(isActive);

		if (CMGroup.GroupType.admin.name().equals(groupType)) {
			builder.withAdminFlag(true); //
		} else if (CMGroup.GroupType.restrictedAdmin.name().equals(groupType)) {
			builder.withAdminFlag(true);
			builder.withRestrictedAdminFlag(true);
		}

		if (newGroup) {
			final GroupDTO groupDTO = builder.build();
			createdOrUpdatedGroup = groupsLogic().createGroup(groupDTO);
		} else {
			final GroupDTO groupDTO = builder.withGroupId(groupId).build();
			createdOrUpdatedGroup = groupsLogic().updateGroup(groupDTO);
		}

		final JSONObject out = new JSONObject();
		out.put(GROUP, Serializer.serialize(createdOrUpdatedGroup));
		return out;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject enableDisableGroup( //
			@Parameter(IS_ACTIVE) final boolean active, //
			@Parameter(GROUP_ID) final Long groupId) throws JSONException, AuthException {

		final CMGroup group = groupsLogic().setGroupActive(groupId, active);

		final JSONObject out = new JSONObject();
		out.put(GROUP, Serializer.serialize(group));
		return out;
	}

	@Admin
	@JSONExported
	public JSONObject getGroupUserList( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(ALREADY_ASSOCIATED) final boolean associated) throws JSONException {

		final JSONObject out = new JSONObject();
		final AuthenticationLogic authLogic = authLogic();
		final List<CMUser> associatedUsers = authLogic.getUsersForGroupWithId(groupId);

		if (!associated) {
			final List<CMUser> notAssociatedUsers = Lists.newArrayList();
			for (final CMUser user : authLogic.getAllUsers(false)) {
				if (associatedUsers.contains(user)) {
					continue;
				}
				notAssociatedUsers.add(user);
			}
			out.put(USERS, Serializer.serializeUsers(notAssociatedUsers));
		} else {
			out.put(USERS, Serializer.serializeUsers(associatedUsers));
		}

		return out;
	}

	/**
	 *
	 * @param users
	 *            a String of comma separated user identifiers. These are the id
	 *            of the users that belong to the group with id = groupId
	 * @param groupId
	 */
	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUserList( //
			@Parameter(value = USERS, required = false) final String users, //
			@Parameter(GROUP_ID) final Long groupId) { //

		final List<Long> newUserIds = Lists.newArrayList();
		if (!users.isEmpty()) {
			final String[] splittedUserIds = users.split(",");
			for (final String userId : splittedUserIds) {
				newUserIds.add(Long.valueOf(userId));
			}
		}
		final List<Long> oldUserIds = authLogic().getUserIdsForGroupWithId(groupId);
		for (final Long userId : newUserIds) {
			if (!oldUserIds.contains(userId)) {
				groupsLogic().addUserToGroup(userId, groupId);
			}
		}
		for (final Long userId : oldUserIds) {
			if (!newUserIds.contains(userId)) {
				groupsLogic().removeUserFromGroup(userId, groupId);
			}
		}
	}

	/*
	 * UI configuration
	 */

	@JSONExported
	public JsonResponse getUIConfiguration() throws AuthException, ORMException {
		final Long groupId = operationUser().getPreferredGroup().getId();
		final SecurityLogic securityLogic = securityLogic();
		final UIConfiguration uiConfiguration = securityLogic.fetchGroupUIConfiguration(groupId);
		return JsonResponse.success(uiConfiguration);
	}

	@Admin
	@JSONExported
	public JsonResponse getGroupUIConfiguration(@Parameter(ID) final Long groupId) throws AuthException, ORMException {
		final SecurityLogic securityLogic = securityLogic();
		final UIConfiguration uiConfiguration = securityLogic.fetchGroupUIConfiguration(groupId);
		return JsonResponse.success(uiConfiguration);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveGroupUIConfiguration( //
			@Parameter(ID) final Long groupId, //
			@Parameter(UI_CONFIGURATION) final String jsonUIConfiguration //
	) throws AuthException, JsonParseException, JsonMappingException, IOException {

		final SecurityLogic securityLogic = securityLogic();
		final UIConfiguration uiConfiguration = mapper.readValue(jsonUIConfiguration, UIConfiguration.class);
		securityLogic.saveGroupUIConfiguration(groupId, uiConfiguration);
	}

	/*
	 * Privileges
	 */

	@JSONExported
	public JSONObject getClassPrivilegeList( //
			@Parameter(GROUP_ID) final Long groupId //
	) throws JSONException, AuthException {
		final List<PrivilegeInfo> classPrivilegesForGroup = securityLogic().fetchClassPrivilegesForGroup(groupId);
		return PrivilegeSerializer.serializePrivilegeList(classPrivilegesForGroup);
	}

	@JSONExported
	public JSONObject getProcessPrivilegeList( //
			@Parameter(GROUP_ID) final Long groupId //
	) throws JSONException, AuthException {
		final List<PrivilegeInfo> processPrivilegesForGroup = securityLogic().fetchProcessPrivilegesForGroup(groupId);
		return PrivilegeSerializer.serializePrivilegeList(processPrivilegesForGroup);
	}

	@JSONExported
	public JSONObject getViewPrivilegeList( //
			@Parameter(GROUP_ID) final Long groupId //
	) throws JSONException, AuthException {
		final List<PrivilegeInfo> viewPrivilegesForGroup = securityLogic().fetchViewPrivilegesForGroup(groupId);
		return PrivilegeSerializer.serializePrivilegeList(viewPrivilegesForGroup);
	}

	@JSONExported
	public JSONObject getFilterPrivilegeList( //
			@Parameter(GROUP_ID) final Long groupId //
	) throws JSONException, AuthException {
		final List<PrivilegeInfo> filterPrivilegesForGroup = securityLogic().fetchFilterPrivilegesForGroup(groupId);
		return PrivilegeSerializer.serializePrivilegeList(filterPrivilegesForGroup);
	}

	@JSONExported
	public JSONObject getCustomPagePrivilegeList( //
			@Parameter(GROUP_ID) final Long groupId //
	) throws JSONException, AuthException {
		final List<PrivilegeInfo> elements = securityLogic().fetchCustomViewPrivilegesForGroup(groupId);
		return PrivilegeSerializer.serializePrivilegeList(elements);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveClassPrivilege( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(PRIVILEGE_MODE) final String privilegeMode //
	) throws AuthException { //
		final PrivilegeMode mode = extractPrivilegeMode(privilegeMode);
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupId, serializablePrivilege(privilegedObjectId),
				mode, null);
		securityLogic().saveClassPrivilege(privilegeInfoToSave, true);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveProcessPrivilege( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(PRIVILEGE_MODE) final String privilegeMode //
	) throws AuthException { //
		final PrivilegeMode mode = extractPrivilegeMode(privilegeMode);
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupId, serializablePrivilege(privilegedObjectId),
				mode, null);
		securityLogic().saveProcessPrivilege(privilegeInfoToSave, true);
	}

	private SerializablePrivilege serializablePrivilege(final Long privilegedObjectId) {
		final SerializablePrivilege unsupported = UnsupportedProxyFactory.of(SerializablePrivilege.class).create();
		return new ForwardingSerializablePrivilege() {

			@Override
			protected SerializablePrivilege delegate() {
				return unsupported;
			}

			@Override
			public Long getId() {
				return privilegedObjectId;
			}

		};
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveViewPrivilege( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(PRIVILEGE_MODE) final String privilegeMode) throws AuthException {
		final PrivilegeMode mode = extractPrivilegeMode(privilegeMode);
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupId, serializablePrivilege(privilegedObjectId),
				mode, null);
		securityLogic().saveViewPrivilege(privilegeInfoToSave);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveFilterPrivilege( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(PRIVILEGE_MODE) final String privilegeMode) throws AuthException {
		final PrivilegeMode mode = extractPrivilegeMode(privilegeMode);
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupId, serializablePrivilege(privilegedObjectId),
				mode, null);
		securityLogic().saveFilterPrivilege(privilegeInfoToSave);
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void saveCustomPagePrivilege( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(PRIVILEGE_MODE) final String privilegeMode) throws AuthException {
		final PrivilegeMode mode = extractPrivilegeMode(privilegeMode);
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupId, serializablePrivilege(privilegedObjectId),
				mode, null);
		securityLogic().saveCustomPagePrivilege(privilegeInfoToSave);
	}

	private PrivilegeMode extractPrivilegeMode(final String privilegeMode) {
		PrivilegeMode mode = null;
		if (privilegeMode.equals(PRIVILEGE_WRITE)) {
			mode = PrivilegeMode.WRITE;
		} else if (privilegeMode.equals(PRIVILEGE_READ)) {
			mode = PrivilegeMode.READ;
		} else {
			mode = PrivilegeMode.NONE;
		}
		return mode;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public void setRowAndColumnPrivileges( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(PRIVILEGE_OBJ_ID) final Long privilegedObjectId, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = ATTRIBUTES, required = false) final JSONArray jsonAttributes //
	) throws JSONException, AuthException {

		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo( //
				groupId, //
				serializablePrivilege(privilegedObjectId), //
				null, //
				null //
		);

		// from jsonArray to string array
		final String[] attributes;
		if (jsonAttributes == null) {
			attributes = null;
		} else {
			attributes = new String[jsonAttributes.length()];
			for (int i = 0; i < attributes.length; ++i) {
				attributes[i] = jsonAttributes.getString(i);
			}
		}

		privilegeInfoToSave.setAttributesPrivileges(attributes);
		privilegeInfoToSave.setPrivilegeFilter(filter);

		securityLogic().saveClassPrivilege(privilegeInfoToSave, false);
	}

	/*
	 * User management
	 */

	@JSONExported
	public JSONObject getUserList( //
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException, AuthException {
		final JSONObject out = new JSONObject();
		out.put(ROWS, Serializer.serializeUsers(authLogic().getAllUsers(activeOnly)));
		return out;
	}

	@JSONExported
	public void changePassword(@Parameter(NEW_PASSWORD) final String newPassword,
			@Parameter(OLD_PASSWORD) final String oldPassword) {
		final OperationUser currentLoggedUser = operationUser();
		final boolean passwordChanged = currentLoggedUser.getAuthenticatedUser().changePassword(oldPassword,
				newPassword);
		if (!passwordChanged) {
			throw AuthException.AuthExceptionType.AUTH_WRONG_PASSWORD.createException();
		}
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject saveUser( //
			@Parameter(USER_ID) final Long userId, //
			@Parameter(value = DESCRIPTION, required = false) final String description, //
			@Parameter(value = USER_NAME, required = false) final String username, //
			@Parameter(value = PASSWORD, required = false) final String password, //
			@Parameter(value = EMAIL, required = false) final String email, //
			@Parameter(IS_ACTIVE) final boolean isActive, //
			@Parameter(DEFAULT_GROUP) final Long defaultGroupId, //
			@Parameter(value = SERVICE, required = false) final boolean service, //
			@Parameter(value = PRIVILEGED, required = false) final boolean privileged //
	) throws JSONException, AuthException {
		// TODO: check if password and confirmation match
		final boolean newUser = userId <= -1;
		CMUser createdOrUpdatedUser = null;
		final UserDTOBuilder userDTOBuilder = UserDTO.newInstance() //
				.withDescription(description) //
				.withUsername(username) //
				.withPassword(password) //
				.withEmail(email) //
				.withDefaultGroupId(defaultGroupId) //
				.withActiveStatus(isActive) //
				.withService(service) //
				.withPrivileged(privileged);
		final AuthenticationLogic authLogic = authLogic();
		if (newUser) {
			final UserDTO userDTO = userDTOBuilder.build();
			createdOrUpdatedUser = authLogic.createUser(userDTO);
		} else {
			final UserDTO userDTO = userDTOBuilder.withUserId(userId).build();
			createdOrUpdatedUser = authLogic.updateUser(userDTO);
		}

		final JSONObject out = new JSONObject();
		out.put(ROWS, Serializer.serialize(createdOrUpdatedUser));
		return out;
	}

	/**
	 *
	 * @param serializer
	 * @param userId
	 * @return the groups to which the current user belongs
	 * @throws JSONException
	 */

	@JSONExported
	public JSONObject getUserGroupList( //
			@Parameter(value = USER_ID) final Long userId) //
					throws JSONException {
		final AuthenticationLogic authLogic = authLogic();
		final CMUser user = authLogic.getUserWithId(userId);
		final List<GroupInfo> groupsForLogin = Lists.newArrayList();
		for (final String name : user.getGroupNames()) {
			groupsForLogin.add(authLogic.getGroupInfoForGroup(name));
		}
		final JSONArray jsonGroupList = Serializer.serializeGroupsForUser(user, groupsForLogin);

		final JSONObject out = new JSONObject();
		out.put(RESULT, jsonGroupList);
		return out;
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JSONObject disableUser( //
			@Parameter(USER_ID) final Long userId, //
			@Parameter(DISABLE) final boolean disable) throws JSONException, AuthException {

		final AuthenticationLogic authLogic = authLogic();
		CMUser user;
		if (disable) {
			user = authLogic.disableUserWithId(userId);
		} else {
			user = authLogic.enableUserWithId(userId);
		}

		final JSONObject out = new JSONObject();
		out.put(ROWS, Serializer.serialize(user));
		return out;
	}

	@JSONExported
	public JsonResponse loadClassUiConfiguration( //
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(CLASS_ID) final Long classId) throws AuthException {

		final CardEditMode cardEditMode = securityLogic().fetchCardEditModeForGroupAndClass(groupId, classId);
		return JsonResponse.success(LOGIC_TO_JSON.apply(cardEditMode));
	}

	@Admin(AdminAccess.DEMOSAFE)
	@JSONExported
	public JsonResponse saveClassUiConfiguration(
			//
			@Parameter(GROUP_ID) final Long groupId, //
			@Parameter(CLASS_ID) final Long classId, //
			@Parameter(CREATE) final boolean disableCreate, //
			@Parameter(MODIFY) final boolean disableUpdate, //
			@Parameter(CLONE) final boolean disableClone, //
			@Parameter(REMOVE) final boolean disableDelete) throws AuthException {

		final CardEditMode cardEditMode = CardEditMode.newInstance() //
				.isCreateAllowed(!disableCreate) //
				.isCloneAllowed(!disableClone) //
				.isUpdateAllowed(!disableUpdate) //
				.isDeleteAllowed(!disableDelete) //
				.build();

		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo( //
				groupId, //
				serializablePrivilege(classId), //
				null, //
				cardEditMode);

		securityLogic().saveCardEditMode(privilegeInfoToSave);
		return JsonResponse.success(null);
	}

	public static final Function<CardEditMode, String> LOGIC_TO_JSON = new Function<CardEditMode, String>() {

		@Override
		public String apply(final CardEditMode input) {
			final String jsonCardEditMode = String.format(CARD_EDIT_MODE_JSON_FORMAT, //
					!input.isAllowUpdate(), //
					!input.isAllowClone(), //
					!input.isAllowRemove(), //
					!input.isAllowCreate());
			return jsonCardEditMode;
		}
	};

}
