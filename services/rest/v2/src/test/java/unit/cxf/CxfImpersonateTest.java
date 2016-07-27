package unit.cxf;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfImpersonate;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;

public class CxfImpersonateTest {

	private static abstract class ForwardingPredicate<T> extends ForwardingObject implements Predicate<T> {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingPredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		@Override
		public boolean apply(final T input) {
			return delegate().apply(input);
		}

	}

	private static class SettablePredicate<T> extends ForwardingPredicate<T> {

		private Predicate<T> delegate;

		public void setDelegate(final Predicate<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<T> delegate() {
			return delegate;
		}

	}

	private static final Predicate<OperationUser> alwaysTrue = alwaysTrue();
	private static final Predicate<OperationUser> alwaysFalse = alwaysFalse();

	private ErrorHandler errorHandler;
	private SessionLogic sessionLogic;
	private SettablePredicate<OperationUser> operationUserAllowed;
	private CxfImpersonate underTest;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		sessionLogic = mock(SessionLogic.class);
		operationUserAllowed = new SettablePredicate<OperationUser>();
		operationUserAllowed.setDelegate(alwaysTrue);
		underTest = new CxfImpersonate(errorHandler, sessionLogic, operationUserAllowed);
	}

	@Test(expected = WebApplicationException.class)
	public void missingSessionThrowsExceptionWhenImpersonating() throws Exception {
		// given
		doReturn(false) //
				.when(sessionLogic).exists(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(eq("token"));

		// when
		underTest.start("token", "user");
	}

	@Test(expected = WebApplicationException.class)
	public void userNotAllowedThrowsExceptionWhenImpersonating() throws Exception {
		// given
		operationUserAllowed.setDelegate(alwaysFalse);
		doReturn(true) //
				.when(sessionLogic).exists(eq("token"));
		doReturn(new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(), new NullGroup())) //
				.when(sessionLogic).getUser(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).notAuthorized();

		// when
		underTest.start("token", "user");
	}

	@Test
	public void userSuccessfullyImpersonated() throws Exception {
		// given
		doReturn(true) //
				.when(sessionLogic).exists(anyString());
		final OperationUser actual = new OperationUser(mock(AuthenticatedUser.class), new NullPrivilegeContext(),
				new NullGroup());
		doReturn(actual) //
				.when(sessionLogic).getUser(anyString());

		// when
		underTest.start("token", "user");

		// then
		verify(sessionLogic).exists(eq("token"));
		verify(sessionLogic).getUser(eq("token"));
		verify(sessionLogic).impersonate(eq("token"), eq("user"));
		verifyNoMoreInteractions(errorHandler, sessionLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void missingSessionThrowsExceptionWhenRestoringPreviousUser() throws Exception {
		// given
		doReturn(false) //
				.when(sessionLogic).exists(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(eq("token"));

		// when
		underTest.stop("token");
	}

	public void userSuccessfullyRestored() throws Exception {
		// given
		doReturn(true) //
				.when(sessionLogic).exists(anyString());

		// when
		underTest.stop("token");

		// then
		verify(sessionLogic).exists(eq("token"));
		verify(sessionLogic).impersonate(eq("token"), null);
		verifyNoMoreInteractions(errorHandler, sessionLogic);
	}

}
