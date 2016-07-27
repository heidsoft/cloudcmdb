package unit.data.store.custompage;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.custompage.DBCustomPage;
import org.cmdbuild.data.store.custompage.FileSystemSynchronizer;
import org.cmdbuild.services.FilesStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class FileSystemSynchronizerTest {

	private static Collection<DBCustomPage> NO_ELEMENTS = emptyList();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private Store<DBCustomPage> store;
	private FilesStore filesStore;
	private FileSystemSynchronizer underTest;

	@Before
	public void setUp() throws Exception {
		store = mock(Store.class);
		filesStore = mock(FilesStore.class);
		underTest = new FileSystemSynchronizer(store, filesStore);
	}

	@Test
	public void whenStoreIsEmptyDirectoryNameIsUsedForCreateNewElement() throws Exception {
		// given
		doReturn(NO_ELEMENTS) //
				.when(store).readAll();
		doReturn(asList(temporaryFolder.newFolder("foo"))) //
				.when(filesStore).files(anyString(), anyString());

		// when
		underTest.synchronize();

		// then
		verify(store).readAll();
		verify(filesStore).files(eq("custompages"), isNull(String.class));
		final ArgumentCaptor<DBCustomPage> captor = ArgumentCaptor.forClass(DBCustomPage.class);
		verify(store).create(captor.capture());

		final DBCustomPage captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
		assertThat(captured.getDescription(), equalTo("foo"));
	}

	@Test
	public void whenStoreIsEmptyFilesAreIgnored() throws Exception {
		// given
		doReturn(NO_ELEMENTS) //
				.when(store).readAll();
		doReturn(asList( //
				temporaryFolder.newFile("foo"), //
				temporaryFolder.newFolder("bar"), //
				temporaryFolder.newFile("baz") //
		)).when(filesStore).files(anyString(), anyString());

		// when
		underTest.synchronize();

		// then
		verify(store).readAll();
		verify(filesStore).files(eq("custompages"), isNull(String.class));
		final ArgumentCaptor<DBCustomPage> captor = ArgumentCaptor.forClass(DBCustomPage.class);
		verify(store).create(captor.capture());

		final DBCustomPage captured = captor.getValue();
		assertThat(captured.getName(), equalTo("bar"));
		assertThat(captured.getDescription(), equalTo("bar"));
	}

	@Test
	public void whenFileSystemIsEmptyStoredElementsAreDeleted() throws Exception {
		final DBCustomPage foo = customPage("foo");
		final DBCustomPage bar = customPage("bar");
		doReturn(asList(foo, bar)) //
				.when(store).readAll();
		doReturn(emptyList()) //
				.when(filesStore).files(anyString(), anyString());

		// when
		underTest.synchronize();

		// then
		verify(store).readAll();
		verify(filesStore).files(eq("custompages"), isNull(String.class));
		final ArgumentCaptor<DBCustomPage> captor = ArgumentCaptor.forClass(DBCustomPage.class);
		verify(store, times(2)).delete(captor.capture());

		final List<DBCustomPage> captured = captor.getAllValues();
		assertThat(captured, hasSize(2));
		assertThat(captured, hasItem(foo));
		assertThat(captured, hasItem(bar));
	}

	@Test
	public void whenElementsExistOnBothEndsFileSystemOnlyElementsAreCreatedStoreOnlyElementsAreDeleted()
			throws Exception {
		// given
		final DBCustomPage foo = customPage("foo");
		final DBCustomPage bar = customPage("bar");
		doReturn(asList(foo, bar)) //
				.when(store).readAll();
		doReturn(asList(temporaryFolder.newFolder("bar"), temporaryFolder.newFolder("baz"))) //
				.when(filesStore).files(anyString(), anyString());

		// when
		underTest.synchronize();

		// then
		verify(store).readAll();
		verify(filesStore).files(eq("custompages"), isNull(String.class));
		final ArgumentCaptor<DBCustomPage> createCaptor = ArgumentCaptor.forClass(DBCustomPage.class);
		verify(store).create(createCaptor.capture());
		final ArgumentCaptor<DBCustomPage> deleteCaptor = ArgumentCaptor.forClass(DBCustomPage.class);
		verify(store).delete(deleteCaptor.capture());

		final DBCustomPage created = createCaptor.getValue();
		assertThat(created.getName(), equalTo("baz"));
		assertThat(created.getDescription(), equalTo("baz"));

		final DBCustomPage deleted = deleteCaptor.getValue();
		assertThat(deleted, equalTo(foo));
	}

	/*
	 * Utilities
	 */

	private DBCustomPage customPage(final String name) {
		final DBCustomPage foo = mock(DBCustomPage.class, name);
		doReturn(name) //
				.when(foo).getName();
		return foo;
	}

}
