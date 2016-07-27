package integration.logic.security;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;

import java.util.List;

import org.cmdbuild.auth.acl.ForwardingSerializablePrivilege;
import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.logic.privileges.DefaultSecurityLogic;
import org.cmdbuild.logic.privileges.PrivilegeInfo;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import utils.IntegrationTestBase;
import utils.UserRolePrivilegeFixture;

public class SecurityLogicTest extends IntegrationTestBase {

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String SIMPLE_USERNAME = "simple_user";
	private static final String SIMPLE_PASSWORD = "simple_password";
	private static final String USER_DEFAULT_GROUP = "userdef";
	private static final String PASSWORD_DEFAULT_GROUP = "userdef_password";

	private UserRolePrivilegeFixture fixture;

	private DBCard admin;
	private DBCard simpleUser;
	private DBCard userWithDefaultGroup;
	private DBCard groupA;
	private DBCard groupB;
	private SecurityLogic securityLogic;

	@Before
	public void setUp() {
		fixture = new UserRolePrivilegeFixture(dbDriver());

		securityLogic = new DefaultSecurityLogic(dbDataView(), null, null, null);
		populateDatabaseWithUsersGroupsAndPrivileges();
	}

	private void populateDatabaseWithUsersGroupsAndPrivileges() {
		admin = fixture.insertUserWithUsernameAndPassword(ADMIN_USERNAME, ADMIN_PASSWORD);
		simpleUser = fixture.insertUserWithUsernameAndPassword(SIMPLE_USERNAME, SIMPLE_PASSWORD);
		userWithDefaultGroup = fixture.insertUserWithUsernameAndPassword(USER_DEFAULT_GROUP, PASSWORD_DEFAULT_GROUP);

		groupA = fixture.insertRoleWithCode("group A");
		groupB = fixture.insertRoleWithCode("group B");

		createUserRoleBinding();
	}

	/**
	 * A user belongs to multiple groups and a group contains more than one user
	 */
	private void createUserRoleBinding() {
		fixture.insertBindingBetweenUserAndRole(admin, groupA);
		fixture.insertBindingBetweenUserAndRole(admin, groupB);
		fixture.insertBindingBetweenUserAndRole(simpleUser, groupB);
		fixture.insertBindingBetweenUserAndRole(userWithDefaultGroup, groupA);
		fixture.insertBindingBetweenUserAndRole(userWithDefaultGroup, groupB, true);
	}

	@Ignore("The Grant class table does not have a history, hence the cm_delete_card does not work")
	@Test
	public void shouldRetrieveAllPrivilegesForGroup() {
		// given
		final DBClass createdClass = dbDriver().createClass(newClass("foo"));

		// when
		final List<PrivilegeInfo> privileges = securityLogic.fetchClassPrivilegesForGroup(groupA.getId());

		// then
		assertEquals(privileges.size(), 1);
		final PrivilegeInfo privilege = privileges.get(0);
		assertThat(privilege.getPrivilegedObjectId(), is(equalTo(createdClass.getId())));
		assertThat(privilege.getGroupId(), is(equalTo(groupA.getId())));
		assertThat(privilege.getMode().getValue(), is(equalTo(PrivilegeMode.WRITE.getValue())));
	}

	@Ignore("The Grant class table does not have a history, hence the cm_delete_card does not work")
	@Test
	public void shouldCreatePrivilegeForExistingClass() {
		// // given
		// final DBClass createdClass = dbDriver().createClass(newClass("foo"));
		// final int numberOfExistentPrivileges =
		// securityLogic.fetchClassPrivilegesForGroup(groupA.getId()).size();
		//
		// // when
		// securityLogic.saveClassPrivilege(groupA.getId(),
		// createdClass.getId(), PrivilegeMode.READ);
		// final PrivilegeInfo privilegeInfo = new PrivilegeInfo(groupA.getId(),
		// createdClass,
		// PrivilegeMode.READ.getValue());
		//
		// // then
		// final List<PrivilegeInfo> groupPrivileges =
		// securityLogic.fetchClassPrivilegesForGroup(groupA.getId());
		// assertEquals(groupPrivileges.size(), numberOfExistentPrivileges + 1);
		// assertThat(groupPrivileges, hasItem(privilegeInfo));
	}

	@Ignore("Because the update card method is not yet implemented")
	@Test
	public void shouldUpdateExistentPrivilege() {
		// given
		final DBClass createdClass = dbDriver().createClass(newClass("foo"));
		fixture.insertPrivilege(groupA.getId(), createdClass, PrivilegeMode.NONE.getValue());
		final int numberOfExistentPrivileges = securityLogic.fetchClassPrivilegesForGroup(groupA.getId()).size();

		// when
		final PrivilegeInfo privilegeInfoToSave = new PrivilegeInfo(groupA.getId(),
				serializablePrivilege(createdClass.getId()), PrivilegeMode.READ, null);
		securityLogic.saveClassPrivilege(privilegeInfoToSave, true);

		// then
		final int numberOfActualPrivileges = securityLogic.fetchClassPrivilegesForGroup(groupA.getId()).size();
		assertEquals(numberOfExistentPrivileges, numberOfActualPrivileges);
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

}
