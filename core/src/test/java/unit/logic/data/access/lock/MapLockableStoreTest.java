package unit.logic.data.access.lock;

import static com.google.common.base.Optional.absent;
import static com.google.common.collect.Iterables.elementsEqual;
import static java.util.Arrays.asList;
import static org.cmdbuild.logic.data.access.lock.Lockables.card;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.logic.data.access.lock.Lockable;
import org.cmdbuild.logic.data.access.lock.LockableStore;
import org.cmdbuild.logic.data.access.lock.MapLockableStore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class MapLockableStoreTest {

	private static class DummyLock implements LockableStore.Lock {

	}

	private static final Optional<DummyLock> ABSENT = absent();

	private Map<Lockable, DummyLock> map;
	private MapLockableStore<DummyLock> store;

	@Before
	public void setUp() throws Exception {
		map = mock(Map.class);
		store = new MapLockableStore<DummyLock>(map);
	}

	@Test
	public void elementStored() throws Exception {
		// given
		final Lockable element = card(1L);
		final DummyLock metadata = new DummyLock();

		// when
		store.add(element, metadata);

		// then
		verify(map).put(eq(element), eq(metadata));
		verifyNoMoreInteractions(map);
	}

	@Test
	public void elementRemoved() throws Exception {
		// given
		final Lockable element = card(1L);

		// when
		store.remove(element);

		// then
		verify(map).remove(eq(element));
		verifyNoMoreInteractions(map);
	}

	@Test
	public void checkIfMissingElementIsPresent() throws Exception {
		// given
		final Lockable element = card(1L);
		doReturn(null) //
				.when(map).get(any(Lockable.class));

		// when
		final boolean present = store.isPresent(element);

		// then
		verify(map).get(eq(element));
		verifyNoMoreInteractions(map);
		assertThat(present, equalTo(false));
	}

	@Test
	public void checkIfAnAlreadyStoredElementIsPresent() throws Exception {
		// given
		final Lockable element = card(1L);
		final DummyLock metadata = new DummyLock();
		doReturn(metadata) //
				.when(map).get(any(Lockable.class));

		// when
		final boolean present = store.isPresent(element);

		// then
		verify(map).get(eq(element));
		verifyNoMoreInteractions(map);
		assertThat(present, equalTo(true));
	}

	@Test
	public void missingElementRetrieved() throws Exception {
		// given
		final Lockable element = card(1L);
		doReturn(null) //
				.when(map).get(any(Lockable.class));

		// when
		final Optional<DummyLock> metadata = store.get(element);

		// then
		verify(map).get(eq(element));
		verifyNoMoreInteractions(map);
		assertThat(metadata, equalTo(ABSENT));
	}

	@Test
	public void alreadyStoredElementRetrieved() throws Exception {
		// given
		final Lockable element = card(1L);
		final DummyLock metadata = new DummyLock();
		doReturn(metadata) //
				.when(map).get(any(Lockable.class));

		// when
		final Optional<DummyLock> _metadata = store.get(element);

		// then
		verify(map).get(eq(element));
		verifyNoMoreInteractions(map);
		assertThat(_metadata.get(), equalTo(metadata));
	}

	@Test
	public void allElementsRetrieved() throws Exception {
		// given
		final Set<Lockable> elements = new HashSet<>(asList(card(1L)));
		doReturn(elements) //
				.when(map).keySet();

		// when
		final Iterable<Lockable> _elements = store.stored();

		// then
		verify(map).keySet();
		verifyNoMoreInteractions(map);
		assertThat(_elements != elements, equalTo(true));
		assertThat(elementsEqual(_elements, elements), equalTo(true));
	}

	@Test
	public void allElementsRemoved() throws Exception {
		// when
		store.removeAll();

		// then
		verify(map).clear();
		verifyNoMoreInteractions(map);
	}

}
