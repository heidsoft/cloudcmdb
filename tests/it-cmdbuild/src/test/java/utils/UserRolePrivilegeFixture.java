package utils;

import static org.cmdbuild.common.Constants.ROLE_CLASS_NAME;

import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.privileges.constants.GrantConstants;
import org.cmdbuild.common.digest.Base64Digester;
import org.cmdbuild.common.digest.Digester;
import org.cmdbuild.dao.Const.Role;
import org.cmdbuild.dao.Const.User;
import org.cmdbuild.dao.Const.UserRole;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UserRolePrivilegeFixture {

	private static final String USER_CLASS = "User";
	private static final String USER_ROLE_DOMAIN = "UserRole";
	private static final Digester digester = new Base64Digester();

	private final List<Long> insertedGroupIds = Lists.newArrayList();
	private final List<Long> insertedUserIds = Lists.newArrayList();
	private final Map<Long, List<Long>> userIdToGroupIds = Maps.newHashMap();

	private final DBDriver driver;

	public UserRolePrivilegeFixture(final DBDriver driver) {
		this.driver = driver;
	}

	public DBCard insertUserWithUsernameAndPassword(final String username, final String password) {
		final DBClass users = getUserClass();
		final DBCard user = DBCard.newInstance(driver, users);
		final DBCard insertedUser = user.set(User.USERNAME, username) //
				.set(User.PASSWORD, digester.encrypt(password)) //
				.set(User.EMAIL, username + "@example.com") //
				.set(User.ACTIVE, true) //
				.set(User.DESCRIPTION, username) //
				.save();
		insertedUserIds.add(insertedUser.getId());
		return insertedUser;
	}

	public DBCard insertRoleWithCode(final String code) {
		final DBClass roles = getRoleClass();
		final DBCard group = DBCard.newInstance(driver, roles);
		final DBCard insertedGroup = (DBCard) group.setCode(code).set(Role.ACTIVE, true).save();
		insertedGroupIds.add(insertedGroup.getId());
		return insertedGroup;
	}

	public DBCard insertPrivilege(final Long roleId, final DBClass clazz, final String mode) {
		final DBClass grantClass = driver.findClass(GrantConstants.GRANT_CLASS_NAME);
		final DBCard privilege = DBCard.newInstance(driver, grantClass);
		final DBCard insertedGrant = privilege.set(GrantConstants.GROUP_ID_ATTRIBUTE, roleId) //
				.set(GrantConstants.PRIVILEGED_CLASS_ID_ATTRIBUTE, clazz.getId()) //
				.set(GrantConstants.MODE_ATTRIBUTE, mode) //
				.save();
		return insertedGrant;
	}

	public DBRelation insertBindingBetweenUserAndRole(final DBCard user, final DBCard role) {
		final DBDomain userRoleDomain = getUserRoleDomain();
		final DBRelation relation = DBRelation.newInstance(driver, userRoleDomain);
		relation.setCard1(user);
		relation.setCard2(role);

		List<Long> groupIds = userIdToGroupIds.get(user.getId());
		if (groupIds == null) {
			groupIds = Lists.newArrayList();
		}
		groupIds.add(role.getId());
		userIdToGroupIds.put(user.getId(), groupIds);

		return relation.save();
	}

	public DBRelation insertBindingBetweenUserAndRole(final DBCard user, final DBCard role, final boolean isDefault) {
		final DBDomain userRoleDomain = driver.findDomain(USER_ROLE_DOMAIN);
		final DBRelation relation = DBRelation.newInstance(driver, userRoleDomain);
		relation.setCard1(user);
		relation.setCard2(role);
		relation.set(UserRole.DEFAULT_GROUP, isDefault);

		List<Long> groupIds = userIdToGroupIds.get(user.getId());
		if (groupIds == null) {
			groupIds = Lists.newArrayList();
		}
		groupIds.add(role.getId());
		userIdToGroupIds.put(user.getId(), groupIds);

		return relation.save();
	}

	public Map<Long, List<Long>> userIdToGroupIds() {
		return userIdToGroupIds;
	}

	public DBClass getUserClass() {
		return driver.findClass(USER_CLASS);
	}

	public DBClass getRoleClass() {
		return driver.findClass(ROLE_CLASS_NAME);
	}

	public DBDomain getUserRoleDomain() {
		return driver.findDomain(USER_ROLE_DOMAIN);
	}

}
