package unit.logic.files;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.cmdbuild.logic.files.CacheExpiration;
import org.cmdbuild.logic.files.CachedFileSystemFacade;
import org.cmdbuild.logic.files.FileSystemFacade;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InOrder;

public class CachedFileSystemFacadeTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void cacheIsNeverUsedForRoot() throws Exception {
		// given
		final File foo = tmp.newFile();
		final FileSystemFacade notCached = mock(FileSystemFacade.class);
		doReturn(foo) //
				.when(notCached).root();
		final CachedFileSystemFacade cached = new CachedFileSystemFacade(notCached, new CacheExpiration() {

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
		final File first = cached.root();
		final File second = cached.root();

		// then
		verify(notCached, times(2)).root();

		assertThat(first, equalTo(foo));
		assertThat(second, equalTo(foo));
	}

	@Test
	public void cacheIsDisabledDirectories() throws Exception {
		// given
		final File foo = tmp.newFile();
		final FileSystemFacade notCached = mock(FileSystemFacade.class);
		doReturn(asList(foo)) //
				.when(notCached).directories();
		final CachedFileSystemFacade cached = new CachedFileSystemFacade(notCached, new CacheExpiration() {

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
		final Collection<File> first = cached.directories();
		final Collection<File> second = cached.directories();

		// then
		verify(notCached, times(2)).directories();

		assertThat(first, contains(foo));
		assertThat(second, contains(foo));
	}

	@Test
	public void cacheIsUsedForDirectories() throws Exception {
		// given
		final File foo = tmp.newFile();
		final FileSystemFacade notCached = mock(FileSystemFacade.class);
		doReturn(asList(foo)) //
				.when(notCached).directories();
		final CachedFileSystemFacade cached = new CachedFileSystemFacade(notCached, new CacheExpiration() {

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
		final Collection<File> first = cached.directories();
		final Collection<File> second = cached.directories();

		// then
		verify(notCached).directories();

		assertThat(first, contains(foo));
		assertThat(second, contains(foo));
	}

	@Test
	public void cacheIsInvalidatedOnSave() throws Exception {
		// given
		final FileSystemFacade notCached = mock(FileSystemFacade.class);
		final File foo = tmp.newFile();
		doReturn(asList(foo)) //
				.when(notCached).directories();
		final File bar = tmp.newFile();
		doReturn(bar) //
				.when(notCached).save(any(DataHandler.class), anyString());
		final CachedFileSystemFacade cached = new CachedFileSystemFacade(notCached, new CacheExpiration() {

			@Override
			public long duration() {
				return 10;
			}

			@Override
			public TimeUnit unit() {
				return MINUTES;
			}

		});
		final DataHandler dataHandler = new DataHandler(new FileDataSource(tmp.newFile()));

		// when
		final Collection<File> first = cached.directories();
		final Collection<File> second = cached.directories();
		final File saved = cached.save(dataHandler, "foo/bar/baz");
		final Collection<File> third = cached.directories();

		// then
		final InOrder inOrder = inOrder(notCached);
		inOrder.verify(notCached).directories();
		inOrder.verify(notCached).save(eq(dataHandler), eq("foo/bar/baz"));
		inOrder.verify(notCached).directories();
		inOrder.verifyNoMoreInteractions();

		assertThat(first, contains(foo));
		assertThat(second, contains(foo));
		assertThat(saved, equalTo(bar));
		assertThat(third, contains(foo));
	}

}
