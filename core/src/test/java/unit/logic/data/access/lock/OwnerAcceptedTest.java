package unit.logic.data.access.lock;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.cmdbuild.logic.data.access.lock.DefaultLockManager.Lock;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.Owner;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.OwnerAccepted;
import org.junit.Test;

import com.google.common.base.Predicate;

public class OwnerAcceptedTest {

	@Test
	public void ownerAccepted() throws Exception {
		// given
		final Predicate<Owner> delegate = mock(Predicate.class);
		doReturn(true) //
				.when(delegate).apply(any(Owner.class));
		final OwnerAccepted underTest = new OwnerAccepted(delegate);
		final Owner owner = mock(Owner.class);
		final Lock lock = Lock.newInstance() //
				.withOwner(owner) //
				.build();

		// when
		final boolean output = underTest.apply(lock);

		// then
		verify(delegate).apply(eq(owner));
		assertThat(output, equalTo(true));
	}

	@Test
	public void ownerNotAccepted() throws Exception {
		// given
		final Predicate<Owner> delegate = mock(Predicate.class);
		doReturn(false) //
				.when(delegate).apply(any(Owner.class));
		final OwnerAccepted underTest = new OwnerAccepted(delegate);
		final Owner owner = mock(Owner.class);
		final Lock lock = Lock.newInstance() //
				.withOwner(owner) //
				.build();

		// when
		final boolean output = underTest.apply(lock);

		// then
		verify(delegate).apply(eq(owner));
		assertThat(output, equalTo(false));
	}

}
