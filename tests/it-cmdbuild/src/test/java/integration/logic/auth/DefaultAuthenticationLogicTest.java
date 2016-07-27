package integration.logic.auth;

import static org.cmdbuild.auth.UserStores.inMemory;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.DefaultPrivilegeContextFactory;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.auth.user.AnonymousUser;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.Response;
import org.cmdbuild.logic.auth.DefaultAuthenticationLogic;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.privileges.DBGroupFetcher;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import utils.IntegrationTestBase;
import utils.UserRolePrivilegeFixture;

public class DefaultAuthenticationLogicTest extends IntegrationTestBase {

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_EMAIL = ADMIN_USERNAME + "@example.com";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String WRONG_ADMIN_PASSWORD = "wrong_password";
	private static final String SIMPLE_USERNAME = "simple_user";
	private static final String SIMPLE_PASSWORD = "simple_password";
	private static final String USER_DEFAULT_GROUP = "userdef";
	private static final String PASSWORD_DEFAULT_GROUP = "userdef_password";

	private static OperationUser anonymous() {
		return new OperationUser(new AnonymousUser(), new SystemPrivilegeContext(), new NullGroup());
	}

	private UserRolePrivilegeFixture fixture;

	private AuthenticationLogic authLogic;
	private DBCard admin;
	private DBCard simpleUser;
	private DBCard userWithDefaultGroup;
	private DBCard groupA;
	private DBCard groupB;
	private DBCard emptyGroup;
	private UserStore IN_MEMORY_STORE;

	@Before
	public void setUp() {
		fixture = new UserRolePrivilegeFixture(dbDriver());

		final AuthenticationService service = new DefaultAuthenticationService(dbDataView());
		final LegacyDBAuthenticator dbAuthenticator = new LegacyDBAuthenticator(dbDataView());
		service.setPasswordAuthenticators(dbAuthenticator);
		service.setUserFetchers(dbAuthenticator);
		service.setGroupFetcher(new DBGroupFetcher(dbDataView(), Lists.<PrivilegeFetcherFactory> newArrayList()));
		IN_MEMORY_STORE = inMemory(operationUser());
		authLogic = new DefaultAuthenticationLogic(service, new DefaultPrivilegeContextFactory(), dbDataView(),
				IN_MEMORY_STORE);

		populateDatabaseWithUsersGroupsAndPrivileges();
	}

	private void populateDatabaseWithUsersGroupsAndPrivileges() {
		admin = fixture.insertUserWithUsernameAndPassword(ADMIN_USERNAME, ADMIN_PASSWORD);
		simpleUser = fixture.insertUserWithUsernameAndPassword(SIMPLE_USERNAME, SIMPLE_PASSWORD);
		userWithDefaultGroup = fixture.insertUserWithUsernameAndPassword(USER_DEFAULT_GROUP, PASSWORD_DEFAULT_GROUP);

		groupA = fixture.insertRoleWithCode("group A");
		groupB = fixture.insertRoleWithCode("group B");
		emptyGroup = fixture.insertRoleWithCode("group C");

		createUserRoleBinding();
	}

	@After
	public void clearSystemTables() throws Exception {
		dbDataView().clear(fixture.getUserRoleDomain());
		dbDataView().clear(fixture.getRoleClass());
		dbDataView().clear(fixture.getUserClass());
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

	@Test
	public void shouldAuthenticateUserWithValidUsernameAndPasswordAndGroupSelected() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString(ADMIN_USERNAME) //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.build();

		// when
		final Response response = authLogic.login(loginDTO, IN_MEMORY_STORE);

		// then
		assertUserIsSuccessfullyAuthenticated(response);
		assertOperationUserIsStoredInUserStore();
	}

	private void assertUserIsSuccessfullyAuthenticated(final Response response) {
		assertTrue(response.isSuccess());
		assertThat(response.getGroupsInfo(), is(nullValue()));
		assertThat(response.getReason(), is(nullValue()));
	}

	private void assertOperationUserIsStoredInUserStore() {
		assertThat(IN_MEMORY_STORE.getUser(), is(not(nullValue())));
	}

	@Test(expected = AuthException.class)
	public void shouldNotAuthenticateUserWithValidEmailAndPasswordAndGroupSelected() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString(ADMIN_EMAIL) //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.build();
		// when
		final Response response = authLogic.login(loginDTO, inMemory(anonymous()));

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getReason(), containsString(AuthExceptionType.AUTH_LOGIN_WRONG.name()));
	}

	@Test
	public void userShouldSelectAGroupIfBelongsToMultipleGroupsAndNoDefault() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString(ADMIN_USERNAME) //
				.withPassword(ADMIN_PASSWORD) //
				.build();

		// when
		final Response response = authLogic.login(loginDTO, IN_MEMORY_STORE);

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getReason(), containsString(AuthExceptionType.AUTH_MULTIPLE_GROUPS.name()));
		assertThat(response.getGroupsInfo(), is(not(nullValue())));
	}

	private void assertOperationUserIsNotStoredInUserStore() {
		assertThat(IN_MEMORY_STORE.getUser(), is(nullValue()));
	}

	@Test
	public void userShouldNotSelectAGroupIfHasDefaultGroup() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString(USER_DEFAULT_GROUP) //
				.withPassword(PASSWORD_DEFAULT_GROUP) //
				.build();

		// when
		final Response response = authLogic.login(loginDTO, inMemory(anonymous()));

		// then
		assertTrue(response.isSuccess());
		assertOperationUserIsStoredInUserStore();
	}

	@Test
	public void userShouldNotSelectAGroupIfBelongsToOnlyOneGroup() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString(SIMPLE_USERNAME) //
				.withPassword(SIMPLE_PASSWORD) //
				.build();

		// when
		final Response response = authLogic.login(loginDTO, inMemory(anonymous()));

		// then
		assertTrue(response.isSuccess());
		assertOperationUserIsStoredInUserStore();
	}

	@Test(expected = AuthException.class)
	public void shouldNotAuthenticateUserWithWrongPassword() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString(ADMIN_USERNAME) //
				.withPassword(WRONG_ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.build();

		// when
		final Response response = authLogic.login(loginDTO, inMemory(anonymous()));

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getGroupsInfo(), is(nullValue()));
		assertThat(response.getReason(), containsString(AuthExceptionType.AUTH_LOGIN_WRONG.name()));
		assertOperationUserIsNotStoredInUserStore();
	}

	@Test(expected = AuthException.class)
	public void shouldNotAuthenticateUserWithWrongUsername() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstance() //
				.withLoginString("wrong_admin_username") //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.build();
		// when
		final Response response = authLogic.login(loginDTO, inMemory(anonymous()));

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getGroupsInfo(), is(nullValue()));
		assertThat(response.getReason(), is(not(nullValue())));
		assertOperationUserIsNotStoredInUserStore();
	}

	@Test
	public void shouldRetrieveAllGroupsForAUser() {
		// when
		final Iterable<String> groupNames = authLogic.getGroupNamesForUserWithId(admin.getId());

		// then
		assertEquals(Iterables.size(groupNames), groupIdsForUserId(admin.getId()).size());
	}

	private List<Long> groupIdsForUserId(final Long userId) {
		return fixture.userIdToGroupIds().get(userId);
	}

	@Test
	public void shouldRetrieveAllUsersForNonEmptyGroup() {
		// when
		final Iterable<CMUser> users = authLogic.getUsersForGroupWithId(groupA.getId());

		// then
		int usersThatActuallyBelongToGroup = 0;
		for (final Long userId : fixture.userIdToGroupIds().keySet()) {
			final List<Long> groupIds = fixture.userIdToGroupIds().get(userId);
			if (groupIds.contains(groupA.getId())) {
				usersThatActuallyBelongToGroup++;
			}
		}
		assertEquals(usersThatActuallyBelongToGroup, Iterables.size(users));
	}

	@Test
	public void shouldRetrieveNoUserForEmptyGroup() {
		// when
		final Iterable<CMUser> users = authLogic.getUsersForGroupWithId(emptyGroup.getId());

		// then
		assertEquals(users.iterator().hasNext(), false);
	}

	@Test
	public void shouldRetrieveUserFromId() {
		// given
		final Long expectedId = admin.getId();

		// when
		final CMUser retrievedUser = authLogic.getUserWithId(expectedId);

		// then
		assertEquals(expectedId, retrievedUser.getId());
	}

	@Test
	public void shouldRetrieveAllGroups() {
		// when
		final Iterable<CMGroup> allGroups = authLogic.getAllGroups();

		// then
		assertEquals(Iterables.size(allGroups), 3);
	}

	@Test
	public void shouldRetrieveExistentGroupFromId() {
		// when
		final CMGroup retrievedGroup = authLogic.getGroupWithId(groupA.getId());

		// then
		assertNotNull(retrievedGroup);
		assertEquals(groupA.getId(), retrievedGroup.getId());
	}

	@Test
	public void shouldRetrieveNullGroupIfNonExistentId() {
		// when
		final CMGroup retrievedGroup = authLogic.getGroupWithId(-1L);

		// then
		assertNotNull(retrievedGroup);
		assertTrue(retrievedGroup instanceof NullGroup);
	}

}
