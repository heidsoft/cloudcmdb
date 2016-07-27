package unit.data.store.custompage;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.custompage.CustomPagesStore;
import org.cmdbuild.data.store.custompage.CustomPagesStore.Synchronizer;
import org.cmdbuild.data.store.custompage.DBCustomPage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class CustomPagesStoreTest {

	private Store<DBCustomPage> store;
	private Synchronizer synchronizer;
	private CustomPagesStore underTest;

	@Before
	public void setUp() throws Exception {
		store = mock(Store.class);
		synchronizer = mock(Synchronizer.class);
		underTest = new CustomPagesStore(store, synchronizer);
	}

	@Test
	public void createDoesNotInvolvesSynchronization() throws Exception {
		// given
		final DBCustomPage element = mock(DBCustomPage.class);

		// when
		underTest.create(element);

		// then
		verify(store).create(eq(element));
		verifyNoMoreInteractions(store, synchronizer);
	}

	@Test
	public void readInvolvesSynchronization() throws Exception {
		// given
		final DBCustomPage element = mock(DBCustomPage.class);

		// when
		underTest.read(element);

		// then
		final InOrder inOrder = inOrder(store, synchronizer);
		inOrder.verify(synchronizer).synchronize();
		inOrder.verify(store).read(eq(element));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void readAllInvolvesSynchronization() throws Exception {
		// when
		underTest.readAll();

		// then
		final InOrder inOrder = inOrder(store, synchronizer);
		inOrder.verify(synchronizer).synchronize();
		inOrder.verify(store).readAll();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void readAllGroupableInvolvesSynchronization() throws Exception {
		// given
		final Groupable groupable = mock(Groupable.class);

		// when
		underTest.readAll(groupable);

		// then
		final InOrder inOrder = inOrder(store, synchronizer);
		inOrder.verify(synchronizer).synchronize();
		inOrder.verify(store).readAll(eq(groupable));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void updateDoesNotInvolvesSynchronization() throws Exception {
		// given
		final DBCustomPage element = mock(DBCustomPage.class);

		// when
		underTest.update(element);

		// then
		verify(store).update(eq(element));
		verifyNoMoreInteractions(store, synchronizer);
	}

	@Test
	public void deleteDoesNotInvolvesSynchronization() throws Exception {
		// given
		final DBCustomPage element = mock(DBCustomPage.class);

		// when
		underTest.delete(element);

		// then
		verify(store).delete(eq(element));
		verifyNoMoreInteractions(store, synchronizer);
	}

}
