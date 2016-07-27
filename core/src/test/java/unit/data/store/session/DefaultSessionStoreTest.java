package unit.data.store.session;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.session.DefaultSessionStore;
import org.cmdbuild.data.store.session.Session;
import org.junit.Before;
import org.junit.Test;

public class DefaultSessionStoreTest {

	private Store<Session> delegate;
	private DefaultSessionStore underTest;

	@Before
	public void setUp() {
		underTest = new DefaultSessionStore(delegate);
	}

	@Test
	public void noUserReturnedWhenBothAreMissing() throws Exception {
		// given
		final Session session = mock(Session.class);

		// when
		final OperationUser response = underTest.selectUserOrImpersonated(session);

		// then
		verify(session).getImpersonated();
		verify(session).getUser();
		verifyNoMoreInteractions(session);

		assertThat(response, nullValue());
	}

	@Test
	public void mainUserReturnedWhenImpersonatedIsMissing() throws Exception {
		// given
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		final OperationUser operationUser = new OperationUser(authenticatedUser, new NullPrivilegeContext(),
				new NullGroup());
		final Session session = mock(Session.class);
		doReturn(operationUser) //
				.when(session).getUser();

		// when
		final OperationUser response = underTest.selectUserOrImpersonated(session);

		// then
		verify(session).getImpersonated();
		verify(session).getUser();
		verifyNoMoreInteractions(session);

		assertThat(response, equalTo(operationUser));
	}

	@Test
	public void impersonatedUserReturnedIfPresent() throws Exception {
		// given
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		final OperationUser operationUser = new OperationUser(authenticatedUser, new NullPrivilegeContext(),
				new NullGroup());
		final Session session = mock(Session.class);
		doReturn(operationUser) //
				.when(session).getImpersonated();

		// when
		final OperationUser response = underTest.selectUserOrImpersonated(session);

		// then
		verify(session).getImpersonated();
		verifyNoMoreInteractions(session);

		assertThat(response, equalTo(operationUser));
	}

}
