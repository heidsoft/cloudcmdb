package unit.logic.data.access.lock;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.cmdbuild.logic.data.access.lock.DisposingLockableStore.PredicateBasedDisposer;
import org.cmdbuild.logic.data.access.lock.Lockable;
import org.cmdbuild.logic.data.access.lock.LockableStore;
import org.cmdbuild.logic.data.access.lock.LockableStore.Lock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.base.Predicate;

public class PredicateBasedDisposerTest {

	private Predicate<Lock> predicate;
	private PredicateBasedDisposer<Lock> disposer;

	@Before
	public void setUp() throws Exception {
		predicate = mock(Predicate.class);
		disposer = new PredicateBasedDisposer<Lock>(predicate);
	}

	@Test
	public void elementNotRemovedIfNotPresent() throws Exception {
		// given
		final Lockable lockable = mock(Lockable.class);
		final LockableStore<Lock> store = mock(LockableStore.class);
		doReturn(absent()) //
				.when(store).get(eq(lockable));

		// when
		disposer.dispose(lockable, store);

		// then
		final InOrder inOrder = inOrder(predicate, store);
		inOrder.verify(store).get(eq(lockable));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void elementNotRemovedIfPresentButPredicateNotApply() throws Exception {
		// given
		final Lockable lockable = mock(Lockable.class);
		final Lock lock = mock(Lock.class);
		final LockableStore<Lock> store = mock(LockableStore.class);
		doReturn(of(lock)) //
				.when(store).get(eq(lockable));
		doReturn(false) //
				.when(predicate).apply(eq(lock));

		// when
		disposer.dispose(lockable, store);

		// then
		final InOrder inOrder = inOrder(predicate, store);
		inOrder.verify(store).get(eq(lockable));
		inOrder.verify(predicate).apply(eq(lock));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void elementRemovedIfPresentAndPredicateApply() throws Exception {
		// given
		final Lockable lockable = mock(Lockable.class);
		final Lock lock = mock(Lock.class);
		final LockableStore<Lock> store = mock(LockableStore.class);
		doReturn(of(lock)) //
				.when(store).get(eq(lockable));
		doReturn(true) //
				.when(predicate).apply(eq(lock));

		// when
		disposer.dispose(lockable, store);

		// then
		final InOrder inOrder = inOrder(predicate, store);
		inOrder.verify(store).get(eq(lockable));
		inOrder.verify(predicate).apply(eq(lock));
		inOrder.verify(store).remove(eq(lockable));
		inOrder.verifyNoMoreInteractions();
	}

}
