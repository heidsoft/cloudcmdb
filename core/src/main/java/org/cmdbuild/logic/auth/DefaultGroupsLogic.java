package org.cmdbuild.logic.auth;

import static org.cmdbuild.common.Constants.ROLE_CLASS_NAME;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.auth.GroupDTO.GroupDTOCreationValidator;
import org.cmdbuild.logic.auth.GroupDTO.GroupDTOUpdateValidator;

public class DefaultGroupsLogic implements GroupsLogic {

	private static final String USER_GROUP_DOMAIN_NAME = "UserRole";

	private final AuthenticationService authenticationService;
	private final CMDataView dataView;
	private final UserStore userStore;

	public DefaultGroupsLogic(final AuthenticationService authenticationService, final CMDataView dataView,
			final UserStore userStore) {
		this.authenticationService = authenticationService;
		this.dataView = dataView;
		this.userStore = userStore;
	}

	@Override
	public CMGroup createGroup(final GroupDTO groupDTO) {
		final ModelValidator<GroupDTO> validator = new GroupDTOCreationValidator();
		if (!validator.validate(groupDTO)) {
			throw ORMExceptionType.ORM_CANT_CREATE_GROUP.createException();
		}

		// the restricted administrator could not create
		// a new group with administrator privileges
		final CMGroup userGroup = getCurrentlyLoggedUserGroup();
		if (userGroup.isRestrictedAdmin() && groupDTO.isAdministrator()) {

			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}

		final String groupName = groupDTO.getName();
		if (!existsGroupWithName(groupName)) {
			return authenticationService.createGroup(groupDTO);
		} else {
			throw ORMExceptionType.ORM_DUPLICATE_GROUP.createException();
		}
	}

	private boolean existsGroupWithName(final String groupName) {
		final CMGroup group = authenticationService.fetchGroupWithName(groupName);
		if (group instanceof NullGroup) {
			return false;
		}
		return true;
	}

	@Override
	public CMGroup updateGroup(final GroupDTO groupDTO) {
		final ModelValidator<GroupDTO> validator = new GroupDTOUpdateValidator();

		if (!validator.validate(groupDTO)) {
			throw ORMExceptionType.ORM_ERROR_CARD_UPDATE.createException();
		}

		final CMGroup userGroup = getCurrentlyLoggedUserGroup();
		final CMGroup groupToUpdate = authenticationService.fetchGroupWithId(groupDTO.getGroupId());

		// the restricted administrator could update only non administrator
		// groups or other restricted groups. In any case it could not set them
		// as full administration.
		if (userGroup.isRestrictedAdmin()) {
			if (groupToUpdate.isAdmin() && !groupToUpdate.isRestrictedAdmin()) {
				throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
			} else if (groupDTO.isAdministrator()) {
				throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
			}
		}

		final CMGroup updatedGroup = authenticationService.updateGroup(groupDTO);
		return updatedGroup;
	}

	@Override
	public CMGroup setGroupActive(final Long groupId, final boolean active) {
		final CMGroup userGroup = getCurrentlyLoggedUserGroup();
		final CMGroup groupToUpdate = authenticationService.fetchGroupWithId(groupId);

		// A group could not activate/deactivate itself
		if (userGroup.getId().equals(groupToUpdate.getId())) {
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}

		// The restricted administrator could
		// activate/deactivate only non administrator groups
		checkRestrictedAdminOverFullAdmin(groupToUpdate.getId());

		return authenticationService.setGroupActive(groupId, active);
	}

	@Override
	public void addUserToGroup(final Long userId, final Long groupId) {
		/*
		 * a restricted administrator could not add a user to a full
		 * administrator group
		 */
		checkRestrictedAdminOverFullAdmin(groupId);

		final CMDomain userRoleDomain = dataView.findDomain(USER_GROUP_DOMAIN_NAME);
		final CMRelationDefinition relationDefinition = dataView.createRelationFor(userRoleDomain);
		relationDefinition.setCard1(fetchUserCardWithId(userId));
		relationDefinition.setCard2(fetchRoleCardWithId(groupId));
		relationDefinition.save();
	}

	private void checkRestrictedAdminOverFullAdmin(final Long groupId) {
		final CMGroup userGroup = getCurrentlyLoggedUserGroup();
		final CMGroup groupToUpdate = authenticationService.fetchGroupWithId(groupId);
		if (userGroup.isRestrictedAdmin() && groupToUpdate.isAdmin() && !groupToUpdate.isRestrictedAdmin()) {

			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}
	}

	private CMGroup getCurrentlyLoggedUserGroup() {
		final OperationUser operationUser = userStore.getUser();
		return operationUser.getPreferredGroup();
	}

	private CMCard fetchUserCardWithId(final Long userId) {
		final CMClass userClass = dataView.findClass("User");
		final CMQueryRow userRow = dataView.select(anyAttribute(userClass)) //
				.from(userClass) //
				.where(condition(QueryAliasAttribute.attribute(userClass, "Id"), eq(userId))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();
		return userRow.getCard(userClass);
	}

	private CMCard fetchRoleCardWithId(final Long groupId) {
		final CMClass roleClass = dataView.findClass(ROLE_CLASS_NAME);
		final CMQueryRow groupRow = dataView.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(QueryAliasAttribute.attribute(roleClass, "Id"), eq(groupId))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();
		return groupRow.getCard(roleClass);
	}

	@Override
	public void removeUserFromGroup(final Long userId, final Long groupId) {
		/*
		 * a restricted administrator could not remove a user from a full
		 * administrator group
		 */
		checkRestrictedAdminOverFullAdmin(groupId);

		final CMDomain userRoleDomain = dataView.findDomain("UserRole");
		final CMClass roleClass = dataView.findClass(ROLE_CLASS_NAME);
		final CMClass userClass = dataView.findClass("User");

		final CMQueryRow row = dataView.select(attribute(userClass, "Username")) //
				.from(userClass) //
				.join(roleClass, over(userRoleDomain)) //
				.where(and(condition(attribute(userClass, "Id"), eq(userId)), //
						condition(attribute(roleClass, "Id"), eq(groupId)))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		final CMRelation relationToBeRemoved = row.getRelation(userRoleDomain).getRelation();
		final CMRelationDefinition relationDefinition = dataView.update(relationToBeRemoved);
		relationDefinition.delete();
	}

}
