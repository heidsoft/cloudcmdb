package unit.logic.files;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.cmdbuild.logic.files.CacheExpiration;
import org.cmdbuild.logic.files.CachedHashing;
import org.cmdbuild.logic.files.Hashing;
import org.junit.Test;

public class CachedHashingTest {

	@Test
	public void cacheIsDisabled() {
		// given
		final Hashing hashing = mock(Hashing.class);
		doReturn("FOO") //
				.when(hashing).hash(anyString());
		final CachedHashing cached = new CachedHashing(hashing, new CacheExpiration() {

			@Override
			public long duration() {
				// cache is disabled
				return 0;
			}

			@Override
			public TimeUnit unit() {
				return MINUTES;
			}

		});

		// when
		final String first = cached.hash("foo");
		final String second = cached.hash("foo");

		// then
		verify(hashing, times(2)).hash(eq("foo"));

		assertThat(first, equalTo("FOO"));
		assertThat(second, equalTo("FOO"));
	}

	@Test
	public void cacheIsUsed() {
		// given
		final Hashing hashing = mock(Hashing.class);
		doReturn("FOO") //
				.when(hashing).hash(anyString());
		final CachedHashing cached = new CachedHashing(hashing, new CacheExpiration() {

			@Override
			public long duration() {
				return 10;
			}

			@Override
			public TimeUnit unit() {
				return MINUTES;
			}

		});

		// when
		final String first = cached.hash("foo");
		final String second = cached.hash("foo");

		// then
		verify(hashing).hash(eq("foo"));

		assertThat(first, equalTo("FOO"));
		assertThat(second, equalTo("FOO"));
	}

}
