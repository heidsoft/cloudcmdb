package unit.data.store;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.cmdbuild.data.store.CachingStore;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.junit.Before;
import org.junit.Test;

import com.google.common.cache.Cache;

public class CachingStoreTest {

	private static class TestDummyStorable implements Storable {

		private final String value;

		public TestDummyStorable(final String value) {
			this.value = value;
		}

		@Override
		public String getIdentifier() {
			return value;
		}

	}

	private static class TestDummyCachingStore extends CachingStore<TestDummyStorable> {

		private final Cache<String, TestDummyStorable> delegate;

		public TestDummyCachingStore(final Cache<String, TestDummyStorable> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Cache<String, TestDummyStorable> delegate() {
			return delegate;
		}

	}

	private Cache<String, TestDummyStorable> cache;
	private CachingStore<TestDummyStorable> underTest;

	@Before
	public void setUp() throws Exception {
		cache = mock(Cache.class);
		underTest = new TestDummyCachingStore(cache);
	}

	@Test
	public void create() throws Exception {
		// given
		final TestDummyStorable storable = new TestDummyStorable("foo");

		// when
		underTest.create(storable);

		// then
		verify(cache).put(eq("foo"), eq(storable));
		verifyNoMoreInteractions(cache);
	}

	@Test(expected = NoSuchElementException.class)
	public void readMissingElement() throws Exception {
		// given
		final TestDummyStorable storable = new TestDummyStorable("foo");
		doReturn(null) //
				.when(cache).getIfPresent(anyString());

		try {
			// when
			underTest.read(storable);
		} finally {
			// then
			verify(cache).getIfPresent(eq("foo"));
			verifyNoMoreInteractions(cache);
		}
	}

	@Test
	public void read() throws Exception {
		// given
		final TestDummyStorable storable = new TestDummyStorable("foo");
		final TestDummyStorable cached = new TestDummyStorable("bar");
		doReturn(cached) //
				.when(cache).getIfPresent(anyString());

		// when
		final TestDummyStorable read = underTest.read(storable);

		// then
		verify(cache).getIfPresent(eq("foo"));
		verifyNoMoreInteractions(cache);

		assertThat(read, equalTo(cached));
	}

	@Test
	public void readAll() throws Exception {
		// given
		final Map<String, TestDummyStorable> map = new ConcurrentHashMap<>();
		map.put("bar", new TestDummyStorable("bar"));
		map.put("baz", new TestDummyStorable("baz"));
		doReturn(map) //
				.when(cache).asMap();

		// when
		final Collection<TestDummyStorable> read = underTest.readAll();

		// then
		verify(cache).asMap();
		verifyNoMoreInteractions(cache);

		assertThat(read, equalTo(map.values()));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void readAllGroupableNotSupported() throws Exception {
		// when
		underTest.readAll(new Groupable() {

			@Override
			public String getGroupAttributeName() {
				return "foo";
			}

			@Override
			public Object getGroupAttributeValue() {
				// TODO Auto-generated method stub
				return "bar";
			}

		});
	}

	@Test(expected = NoSuchElementException.class)
	public void updateMissingValue() throws Exception {
		// given
		final TestDummyStorable storable = new TestDummyStorable("foo");
		doReturn(null) //
				.when(cache).getIfPresent(anyString());

		try {
			// when
			underTest.update(storable);
		} finally {
			// then
			verify(cache).getIfPresent("foo");
			verifyNoMoreInteractions(cache);
		}
	}

	@Test
	public void update() throws Exception {
		// given
		final TestDummyStorable storable = new TestDummyStorable("foo");
		final TestDummyStorable cached = new TestDummyStorable("bar");
		doReturn(cached) //
				.when(cache).getIfPresent(anyString());

		// when
		underTest.update(storable);

		// then
		verify(cache).getIfPresent("foo");
		verify(cache).put("foo", storable);
		verifyNoMoreInteractions(cache);
	}

	@Test(expected = NoSuchElementException.class)
	public void deleteMissingValue() throws Exception {
		// given
		final TestDummyStorable storable = new TestDummyStorable("foo");
		doReturn(null) //
				.when(cache).getIfPresent(anyString());

		try {
			// when
			underTest.delete(storable);
		} finally {
			// then
			verify(cache).getIfPresent("foo");
			verifyNoMoreInteractions(cache);
		}
	}

	public void delete() throws Exception {
		// given
		final TestDummyStorable storable = new TestDummyStorable("foo");
		final TestDummyStorable cached = new TestDummyStorable("bar");
		doReturn(cached) //
				.when(cache).getIfPresent(anyString());

		// when
		underTest.delete(storable);

		// then
		verify(cache).getIfPresent("foo");
		verify(cache).invalidate("foo");
		verifyNoMoreInteractions(cache);
	}

}
