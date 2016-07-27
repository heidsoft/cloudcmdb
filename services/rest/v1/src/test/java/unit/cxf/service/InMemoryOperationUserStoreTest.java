package unit.cxf.service;

import static org.cmdbuild.service.rest.v1.model.Models.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.v1.cxf.service.InMemoryOperationUserStore;
import org.cmdbuild.service.rest.v1.cxf.service.OperationUserStore.BySession;
import org.cmdbuild.service.rest.v1.model.Session;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class InMemoryOperationUserStoreTest {

	private InMemoryOperationUserStore store;

	@Before
	public void setUp() throws Exception {
		store = new InMemoryOperationUserStore();
	}

	@Test(expected = NullPointerException.class)
	public void nullSessionThrowsException() throws Exception {
		// when
		store.of(null);
	}

	public void calledMultipleTimesReturnsSameValue() throws Exception {
		// given
		final Session session = session("id");

		// when
		final BySession first = store.of(session);
		final BySession second = store.of(session);

		// then
		assertThat(first, equalTo(second));
	}

	@Test(expected = NullPointerException.class)
	public void nullMainValueThrowsException() throws Exception {
		// given
		final Session session = session("id");

		// when
		store.of(session).main(null);
	}

	@Test
	public void newlyCreatedDataReturnsAbsent() throws Exception {
		// given
		final Session missing = session("missing");

		// when
		final Optional<OperationUser> shouldBeAbsent = store.of(missing).get();

		// then
		assertThat(shouldBeAbsent.isPresent(), equalTo(false));
	}

	@Test(expected = NullPointerException.class)
	public void nullImpersonateValueThrowsExceptionWhenMainIsMissing() throws Exception {
		// given
		final Session session = session("id");

		// when
		store.of(session).impersonate(null);
	}

	@Test
	public void impersonateValueCanBeNull() throws Exception {
		// given
		final Session session = session("id");

		// when
		store.of(session).main(operationUser());
		store.of(session).impersonate(null);
	}

	@Test
	public void mainValueStoredAndRead() throws Exception {
		// given
		final Session session = session("id");
		final OperationUser value = operationUser();

		// when
		store.of(session).main(value);
		final Optional<OperationUser> stored = store.of(session).get();

		// then
		assertThat(stored.isPresent(), equalTo(true));
		assertThat(stored.get(), equalTo(value));
	}

	@Test
	public void impersonateValueStoredAndRead() throws Exception {
		// given
		final Session session = session("id");
		final OperationUser main = operationUser();
		final OperationUser impersonate = operationUser();

		// when
		store.of(session).main(main);
		store.of(session).impersonate(impersonate);
		final Optional<OperationUser> stored = store.of(session).get();

		// then
		assertThat(stored.isPresent(), equalTo(true));
		assertThat(stored.get(), equalTo(impersonate));
	}

	@Test(expected = NullPointerException.class)
	public void removingNullKeyThrowsException() throws Exception {
		// when
		store.remove(null);
	}

	@Test
	public void removingMissingIdDoesNothing() throws Exception {
		// given
		final Session missing = session("missing");

		// when
		store.remove(missing);
	}

	@Test
	public void putRemoveAndRead() throws Exception {
		// given
		final Session session = session("id");
		final OperationUser value = operationUser();

		// when
		store.of(session).main(value);
		store.remove(session);
		final Optional<OperationUser> stored = store.of(session).get();

		// then
		assertThat(stored.isPresent(), equalTo(false));
	}

	private static Session session(final String id) {
		return newSession().withId(id).build();
	}

	private static OperationUser operationUser() {
		return new OperationUser(mock(AuthenticatedUser.class), mock(PrivilegeContext.class), mock(CMGroup.class));
	}

}
