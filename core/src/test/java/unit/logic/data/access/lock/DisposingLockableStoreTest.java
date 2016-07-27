package unit.logic.data.access.lock;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.List;

import org.cmdbuild.logic.data.access.lock.DisposingLockableStore;
import org.cmdbuild.logic.data.access.lock.DisposingLockableStore.Disposer;
import org.cmdbuild.logic.data.access.lock.Lockable;
import org.cmdbuild.logic.data.access.lock.LockableStore;
import org.cmdbuild.logic.data.access.lock.LockableStore.Lock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class DisposingLockableStoreTest {

	private static class Dummy implements Lock {

	}

	private LockableStore<Dummy> delegate;
	private Disposer<Dummy> disposer;
	private DisposingLockableStore<Dummy> disposingLockableStore;

	@Before
	public void setUp() throws Exception {
		delegate = mock(LockableStore.class);
		disposer = mock(Disposer.class);
		disposingLockableStore = new DisposingLockableStore<Dummy>(delegate, disposer);
	}

	@Test
	public void elementDisposedOnAdd() throws Exception {
		// given
		final Lockable element = mock(Lockable.class);
		final Dummy dummy = new Dummy();

		// when
		disposingLockableStore.add(element, dummy);

		// then
		final InOrder inOrder = inOrder(delegate, disposer);
		inOrder.verify(disposer).dispose(eq(element), eq(delegate));
		inOrder.verify(delegate).add(eq(element), eq(dummy));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void elementDisposedOnRemove() throws Exception {
		// given
		final Lockable element = mock(Lockable.class);

		// when
		disposingLockableStore.remove(element);

		// then
		final InOrder inOrder = inOrder(delegate, disposer);
		inOrder.verify(disposer).dispose(eq(element), eq(delegate));
		inOrder.verify(delegate).remove(eq(element));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void elementDisposedOnIsPresent() throws Exception {
		// given
		final Lockable element = mock(Lockable.class);

		// when
		disposingLockableStore.isPresent(element);

		// then
		final InOrder inOrder = inOrder(delegate, disposer);
		inOrder.verify(disposer).dispose(eq(element), eq(delegate));
		inOrder.verify(delegate).isPresent(eq(element));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void elementDisposedOnGet() throws Exception {
		// given
		final Lockable element = mock(Lockable.class);

		// when
		disposingLockableStore.get(element);

		// then
		final InOrder inOrder = inOrder(delegate, disposer);
		inOrder.verify(disposer).dispose(eq(element), eq(delegate));
		inOrder.verify(delegate).get(eq(element));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void elementDisposedOnStored() throws Exception {
		// given
		final Lockable first = mock(Lockable.class);
		final Lockable second = mock(Lockable.class);
		final Lockable third = mock(Lockable.class);
		final List<Lockable> elements = asList(first, second, third);
		doReturn(elements) //
				.when(delegate).stored();

		// when
		disposingLockableStore.stored();

		// then
		final ArgumentCaptor<Lockable> captor = ArgumentCaptor.forClass(Lockable.class);
		final InOrder inOrder = inOrder(delegate, disposer);
		inOrder.verify(disposer, times(elements.size())).dispose(captor.capture(), eq(delegate));
		inOrder.verify(delegate).stored();
		inOrder.verifyNoMoreInteractions();

		final List<Lockable> captured = captor.getAllValues();
		assertThat(captured.get(0), equalTo(first));
		assertThat(captured.get(1), equalTo(second));
		assertThat(captured.get(2), equalTo(third));
	}

}
