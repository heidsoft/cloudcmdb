package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v2.model.Models.newClassPrivilege;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.GroupImpl;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.privileges.PrivilegeInfo;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfClassPrivileges;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.ClassPrivilege;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.stubbing.answers.ReturnsElementsOf;

public class CxfClassPrivilegesTest {

	private static final CMGroup ADMIN = GroupImpl.newInstance() //
			.withId(123L) //
			.withName("admin") //
			.administrator(true) //
			.build();
	private static final CMGroup NORMAL = GroupImpl.newInstance() //
			.withId(456L) //
			.withName("normal") //
			.build();

	private ErrorHandler errorHandler;
	private AuthenticationLogic authenticationLogic;
	private SecurityLogic securityLogic;
	private DataAccessLogic dataAccessLogic;

	private CxfClassPrivileges cxfClassPrivileges;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		authenticationLogic = mock(AuthenticationLogic.class);
		securityLogic = mock(SecurityLogic.class);
		dataAccessLogic = mock(DataAccessLogic.class);
		cxfClassPrivileges = new CxfClassPrivileges(errorHandler, authenticationLogic, securityLogic, dataAccessLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void roleNotFoundWhenReadingAllPrivileges() throws Exception {
		// given
		doReturn(new NullGroup()) //
				.when(authenticationLogic).getGroupWithName(eq("foo"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).roleNotFound(eq("foo"));

		// when
		cxfClassPrivileges.read("foo");
	}

	@Test
	public void allAvailableClassesWithWriteModeWhenRoleIsAdmin() throws Exception {
		// given
		doReturn(ADMIN) //
				.when(authenticationLogic).getGroupWithName(anyString());
		final CMClass foo = mock(CMClass.class);
		doReturn("foo").when(foo).getName();
		final CMClass bar = mock(CMClass.class);
		doReturn("bar").when(bar).getName();
		doReturn(asList(foo, bar)) //
				.when(dataAccessLogic).findClasses(anyBoolean());

		// when
		final ResponseMultiple<ClassPrivilege> response = cxfClassPrivileges.read("foo");

		// then
		final Iterable<ClassPrivilege> elements = response.getElements();
		assertThat(size(elements), equalTo(2));
		assertThat(get(elements, 0), equalTo(newClassPrivilege() //
				.withId("foo") //
				.withName("foo") //
				.withMode("w") //
				.build()));
		assertThat(get(elements, 1), equalTo(newClassPrivilege() //
				.withId("bar") //
				.withName("bar") //
				.withMode("w") //
				.build()));
		verify(authenticationLogic).getGroupWithName(eq("foo"));
		verify(dataAccessLogic).findClasses(eq(true));
		verifyNoMoreInteractions(errorHandler, authenticationLogic, securityLogic, dataAccessLogic);
	}

	@Test
	public void allAvailableClassesWhenRoleIsNonAdmin() throws Exception {
		// given
		doReturn(NORMAL) //
				.when(authenticationLogic).getGroupWithName(anyString());
		final CMClass foo = mock(CMClass.class);
		doReturn(1L).when(foo).getId();
		doReturn("foo").when(foo).getName();
		final CMClass bar = mock(CMClass.class);
		doReturn(2L).when(bar).getId();
		doReturn("bar").when(bar).getName();
		doReturn(asList(//
				PrivilegeInfo.newInstance() //
						.withPrivilegedObject(foo) //
						.withPrivilegeMode(PrivilegeMode.READ) //
						.build(), //
				PrivilegeInfo.newInstance() //
						.withPrivilegedObject(bar) //
						.withPrivilegeMode(PrivilegeMode.WRITE) //
						.build() //
				)) //
				.when(securityLogic).fetchClassPrivilegesForGroup(anyLong());
		doReturn(false) //
				.when(dataAccessLogic).isProcess(any(CMClass.class));
		doReturn(true) //
				.when(dataAccessLogic).hasClass(anyLong());

		// when
		final ResponseMultiple<ClassPrivilege> response = cxfClassPrivileges.read("foo");

		// then
		final Iterable<ClassPrivilege> elements = response.getElements();
		assertThat(size(elements), equalTo(2));
		assertThat(get(elements, 0), equalTo(newClassPrivilege() //
				.withId("foo") //
				.withName("foo") //
				.withMode("r") //
				.build()));
		assertThat(get(elements, 1), equalTo(newClassPrivilege() //
				.withId("bar") //
				.withName("bar") //
				.withMode("w") //
				.build()));
		verify(authenticationLogic).getGroupWithName(eq("foo"));
		verify(securityLogic).fetchClassPrivilegesForGroup(eq(NORMAL.getId()));
		final ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(dataAccessLogic, times(2)).hasClass(idCaptor.capture());
		verifyNoMoreInteractions(errorHandler, authenticationLogic, securityLogic, dataAccessLogic);
	}

	@Test
	public void classesNotAvailableAreNotReturned() throws Exception {
		// given
		doReturn(NORMAL) //
				.when(authenticationLogic).getGroupWithName(anyString());
		final CMClass foo = mock(CMClass.class);
		doReturn(1L).when(foo).getId();
		doReturn("foo").when(foo).getName();
		final CMClass bar = mock(CMClass.class);
		doReturn(2L).when(bar).getId();
		doReturn("bar").when(bar).getName();
		doReturn(asList(//
				PrivilegeInfo.newInstance() //
						.withPrivilegedObject(foo) //
						.withPrivilegeMode(PrivilegeMode.READ) //
						.build(), //
				PrivilegeInfo.newInstance() //
						.withPrivilegedObject(bar) //
						.withPrivilegeMode(PrivilegeMode.WRITE) //
						.build() //
				)) //
				.when(securityLogic).fetchClassPrivilegesForGroup(anyLong());
		doReturn(false) //
				.when(dataAccessLogic).isProcess(any(CMClass.class));
		doAnswer(new ReturnsElementsOf(asList(true, false))) //
				.when(dataAccessLogic).hasClass(anyLong());

		// when
		final ResponseMultiple<ClassPrivilege> response = cxfClassPrivileges.read("foo");

		// then
		final Iterable<ClassPrivilege> elements = response.getElements();
		assertThat(size(elements), equalTo(1));
		assertThat(get(elements, 0), equalTo(newClassPrivilege() //
				.withId("foo") //
				.withName("foo") //
				.withMode("r") //
				.build()));
		verify(authenticationLogic).getGroupWithName(eq("foo"));
		verify(securityLogic).fetchClassPrivilegesForGroup(eq(NORMAL.getId()));
		final ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(dataAccessLogic, times(2)).hasClass(idCaptor.capture());
		verifyNoMoreInteractions(errorHandler, authenticationLogic, securityLogic, dataAccessLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void roleNotFoundWhenReadingSingle() throws Exception {
		// given
		doReturn(new NullGroup()) //
				.when(authenticationLogic).getGroupWithName(eq("foo"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).roleNotFound(eq("foo"));

		// when
		cxfClassPrivileges.read("foo", "bar");
	}

}
