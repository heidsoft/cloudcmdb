package unit.cxf;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v2.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.v2.model.Models.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfSessions;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Session;
import org.junit.Before;
import org.junit.Test;

public class CxfSessionsTest {

	private ErrorHandler errorHandler;
	private SessionLogic sessionLogic;
	private CxfSessions underTest;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		sessionLogic = mock(SessionLogic.class);
		underTest = new CxfSessions(errorHandler, sessionLogic);

	}

	@Test(expected = WebApplicationException.class)
	public void missingUsernameThrowsExceptionWhenCreatingSession() throws Exception {
		// given
		final Session session = newSession() //
				.withPassword("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingUsername();

		// when
		underTest.create(session);
	}

	@Test(expected = WebApplicationException.class)
	public void blankUsernameThrowsExceptionWhenCreatingSession() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername(" \t") //
				.withPassword("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingUsername();

		// when
		underTest.create(session);
	}

	@Test(expected = WebApplicationException.class)
	public void missingPasswordThrowsExceptionWhenCreatingSession() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingPassword();

		// when
		underTest.create(session);
	}

	@Test(expected = WebApplicationException.class)
	public void blankPasswordThrowsExceptionWhenCreatingSession() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("foo") //
				.withPassword(" \t") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingPassword();

		// when
		underTest.create(session);
	}

	@Test(expected = AuthException.class)
	public void errorOnLogicPropagatedWhenCreatingSession() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("foo") //
				.withPassword("bar") //
				.withRole("baz") //
				.build();
		doThrow(AuthExceptionType.AUTH_LOGIN_WRONG.createException()) //
				.when(sessionLogic)
				.create(eq(LoginDTO.newInstance() //
						.withLoginString("foo") //
						.withPassword("bar") //
						.withGroupName("baz") //
						.withServiceUsersAllowed(true) //
						.build()));

		// when
		underTest.create(session);
	}

	@Test
	public void sessionSuccessfullyCreated() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("username") //
				.withPassword("password") //
				.build();
		doReturn("session id") //
				.when(sessionLogic).create(any(LoginDTO.class));
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		doReturn("foo") //
				.when(authenticatedUser).getUsername();
		doReturn(asList("bar", "baz")) //
				.when(authenticatedUser).getGroupNames();
		final OperationUser operationUser = new OperationUser(authenticatedUser, new NullPrivilegeContext(),
				new NullGroup());
		doReturn(operationUser) //
				.when(sessionLogic).getUser(anyString());

		// when
		final ResponseSingle<Session> response = underTest.create(session);

		// then
		verify(sessionLogic).create(eq(LoginDTO.newInstance() //
				.withLoginString(session.getUsername()) //
				.withPassword(session.getPassword()) //
				.withServiceUsersAllowed(true) //
				.build()));
		verify(sessionLogic).getUser(eq("session id"));
		verifyNoMoreInteractions(errorHandler, sessionLogic);

		assertThat(response.getElement(),
				equalTo(newSession() //
						.withId("session id") //
						.withUsername("foo") //
						.withAvailableRoles(asList("bar", "baz")) //
						.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void missingSessionThrowsExceptionWhenReadingSession() throws Exception {
		// given
		doReturn(false) //
				.when(sessionLogic).exists(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(eq("token"));

		// when
		underTest.read("token");
	}

	@Test
	public void sessionSuccessfullyRead() throws Exception {
		// given
		doReturn(true) //
				.when(sessionLogic).exists(anyString());
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		doReturn("username") //
				.when(authenticatedUser).getUsername();
		doReturn(asList("foo", "bar", "baz")) //
				.when(authenticatedUser).getGroupNames();
		final CMGroup group = mock(CMGroup.class);
		doReturn("group") //
				.when(group).getName();
		doReturn(true) //
				.when(group).isActive();
		final OperationUser operationUser = new OperationUser(authenticatedUser, new NullPrivilegeContext(), group);
		doReturn(operationUser) //
				.when(sessionLogic).getUser(anyString());

		// when
		final ResponseSingle<Session> response = underTest.read("token");

		// then
		verify(sessionLogic).exists(eq("token"));
		verify(sessionLogic).getUser(eq("token"));
		verifyNoMoreInteractions(errorHandler, sessionLogic);

		assertThat(response.getElement(),
				equalTo(newSession() //
						.withId("token") //
						.withUsername("username") //
						.withRole("group") //
						.withAvailableRoles(asList("foo", "bar", "baz")) //
						.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void missingSessionThrowsExceptionWhenUpdatingSession() throws Exception {
		// given
		final Session session = newSession() //
				.withId("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("group") //
				.build();
		doReturn(false) //
				.when(sessionLogic).exists(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(eq("token"));

		// when
		underTest.update("token", session);
	}

	@Test(expected = WebApplicationException.class)
	public void update_invalidGroupThrowsExceptionWhenUpdatingSession() throws Exception {
		// given
		final Session session = newSession() //
				// no group
				.build();
		doReturn(true) //
				.when(sessionLogic).exists(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingParam(eq(GROUP));

		// when
		underTest.update("token", session);
	}

	@Test
	public void sessionSuccessfullyUpdated() throws Exception {
		// given
		doReturn(true) //
				.when(sessionLogic).exists(eq("token"));
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		doReturn("username") //
				.when(authenticatedUser).getUsername();
		doReturn(asList("foo", "bar", "baz")) //
				.when(authenticatedUser).getGroupNames();
		final OperationUser operationUser = new OperationUser(authenticatedUser, new NullPrivilegeContext(),
				new NullGroup());
		doReturn(operationUser) //
				.when(sessionLogic).getUser(anyString());

		// when
		underTest.update("token",
				newSession() //
						.withRole("group") //
						.build());

		// then
		verify(sessionLogic).exists(eq("token"));
		verify(sessionLogic, times(2)).getUser(eq("token"));
		verify(sessionLogic).update(eq("token"),
				eq(LoginDTO.newInstance() //
						.withLoginString("username") //
						.withGroupName("group") //
						.withServiceUsersAllowed(true) //
						.build()));
		verifyNoMoreInteractions(errorHandler, sessionLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void missingSessionThrowsExceptionWhenDeletingSession() throws Exception {
		// given
		doReturn(false) //
				.when(sessionLogic).exists(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(eq("token"));

		// when
		underTest.delete("token");
	}

	@Test
	public void sessionSuccessfullyDeleted() throws Exception {
		// given
		doReturn(true) //
				.when(sessionLogic).exists(anyString());

		// when
		underTest.delete("token");

		// then
		verify(sessionLogic).exists(eq("token"));
		verify(sessionLogic).delete(eq("token"));
		verifyNoMoreInteractions(errorHandler, sessionLogic);
	}

}
