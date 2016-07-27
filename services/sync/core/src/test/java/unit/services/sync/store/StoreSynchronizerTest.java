package unit.services.sync.store;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.cmdbuild.services.sync.store.Entry;
import org.cmdbuild.services.sync.store.Key;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.StoreSynchronizer;
import org.cmdbuild.services.sync.store.StoreSynchronizer.Builder;
import org.cmdbuild.services.sync.store.Type;
import org.junit.Test;
import org.mockito.InOrder;

public class StoreSynchronizerTest {

	private static final Iterable<Entry> NO_ENTRIES = Collections.emptyList();

	@Test(expected = NullPointerException.class)
	public void leftStoreIsRequired() throws Exception {
		// given
		final Store right = mock(Store.class);
		final Store target = mock(Store.class);
		final Builder builder = StoreSynchronizer.newInstance() //
				.withRight(right) //
				.withTarget(target);

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void rightStoreIsRequired() throws Exception {
		// given
		final Store left = mock(Store.class);
		final Store target = mock(Store.class);
		final Builder builder = StoreSynchronizer.newInstance() //
				.withLeft(left) //
				.withTarget(target);

		// when
		builder.build();
	}

	@Test(expected = NullPointerException.class)
	public void targetStoreIsRequired() throws Exception {
		// given
		final Store left = mock(Store.class);
		final Store target = mock(Store.class);
		final Builder builder = StoreSynchronizer.newInstance() //
				.withLeft(left) //
				.withTarget(target);

		// when
		builder.build();
	}

	@Test
	public void storesAreTheOnlyRequiredElements() throws Exception {
		// given
		final Store left = mock(Store.class);
		final Store right = mock(Store.class);
		final Store target = mock(Store.class);
		final Builder builder = StoreSynchronizer.newInstance() //
				.withLeft(left) //
				.withRight(right) //
				.withTarget(target);

		// when
		builder.build();
	}

	@Test
	public void leftOnlyEntriesAreCreated() throws Exception {
		// given
		final Key key = mock(Key.class);
		final Entry leftOnlyEntry = mock(Entry.class);
		when(leftOnlyEntry.getKey()) //
				.thenReturn(key);
		final Store left = mock(Store.class);
		when(left.readAll()) //
				.thenReturn(listOf(leftOnlyEntry));
		final Store right = mock(Store.class);
		when(right.readAll()) //
				.thenReturn(NO_ENTRIES);
		final Store target = mock(Store.class);
		final StoreSynchronizer synchronizer = StoreSynchronizer.newInstance() //
				.withLeft(left) //
				.withRight(right) //
				.withTarget(target) //
				.build();

		// when
		synchronizer.sync();

		// then
		final InOrder inOrder = inOrder(left, right, target);
		inOrder.verify(left).readAll();
		inOrder.verify(right).readAll();
		inOrder.verify(target).create(leftOnlyEntry);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void entriesOnBothLeftAndRightAreUpdated() throws Exception {
		// given
		final Key key = mock(Key.class);
		final Entry leftEntry = mock(Entry.class);
		when(leftEntry.getKey()) //
				.thenReturn(key);
		final Store left = mock(Store.class);
		when(left.readAll()) //
				.thenReturn(listOf(leftEntry));
		final Entry rightEntry = mock(Entry.class);
		when(rightEntry.getKey()) //
				.thenReturn(key);
		final Store right = mock(Store.class);
		when(right.readAll()) //
				.thenReturn(listOf(rightEntry));
		final Store target = mock(Store.class);
		final StoreSynchronizer synchronizer = StoreSynchronizer.newInstance() //
				.withLeft(left) //
				.withRight(right) //
				.withTarget(target) //
				.build();

		// when
		synchronizer.sync();

		// then
		final InOrder inOrder = inOrder(left, right, target);
		inOrder.verify(left).readAll();
		inOrder.verify(right).readAll();
		inOrder.verify(target).update(leftEntry);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void rightOnlyEntriesAreDeleted() throws Exception {
		// given
		final Store left = mock(Store.class);
		when(left.readAll()) //
				.thenReturn(NO_ENTRIES);
		final Key key = mock(Key.class);
		final Entry rightOnlyEntry = mock(Entry.class);
		when(rightOnlyEntry.getKey()) //
				.thenReturn(key);
		final Store right = mock(Store.class);
		when(right.readAll()) //
				.thenReturn(listOf(rightOnlyEntry));
		final Store target = mock(Store.class);
		final StoreSynchronizer synchronizer = StoreSynchronizer.newInstance() //
				.withLeft(left) //
				.withRight(right) //
				.withTarget(target) //
				.build();

		// when
		synchronizer.sync();

		// then
		final InOrder inOrder = inOrder(left, right, target);
		inOrder.verify(left).readAll();
		inOrder.verify(right).readAll();
		inOrder.verify(target).delete(rightOnlyEntry);
		inOrder.verifyNoMoreInteractions();
	}

	private Iterable<Entry> listOf(final Entry entry) {
		return Arrays.<Entry> asList(entry);
	}

}
