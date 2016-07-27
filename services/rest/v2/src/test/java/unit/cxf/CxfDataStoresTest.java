package unit.cxf;

import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.cmdbuild.service.rest.v2.model.Models.newFileSystemObject;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.cmdbuild.logic.files.Element;
import org.cmdbuild.logic.files.FileLogic;
import org.cmdbuild.logic.files.FileStore;
import org.cmdbuild.service.rest.v2.cxf.CxfFileStores;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.FileSystemObject;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CxfDataStoresTest {

	private static class DummyException extends Exception {

		private static final long serialVersionUID = 1L;

	}

	private static final String A_FILESTORE = "a filestore";
	private static final String A_FOLDER = "a folder";
	private static final String A_FILE = "a file";

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private ErrorHandler errorHandler;
	private FileLogic fileLogic;
	private FileStore fileStore;
	private CxfFileStores underTest;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		fileStore = mock(FileStore.class);
		fileLogic = mock(FileLogic.class);
		doReturn(fileStore) //
				.when(fileLogic).fileStore(anyString());
		underTest = new CxfFileStores(errorHandler, fileLogic);
	}

	@Test(expected = DummyException.class)
	public void readFoldersCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(fileLogic).fileStore(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFolders(A_FILESTORE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(errorHandler).dataStoreNotFound(eq(A_FILESTORE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test
	public void readFolders() throws Exception {
		// given
		final Element firstFolder = element(temporaryFolder.newFolder());
		final Element secondFolder = element(temporaryFolder.newFolder());
		final Element thirdFolder = element(temporaryFolder.newFolder());
		doReturn(asList(firstFolder, secondFolder, thirdFolder)) //
				.when(fileStore).folders();

		// when
		final ResponseMultiple<FileSystemObject> response = underTest.readFolders(A_FILESTORE);

		// then
		verify(fileLogic).fileStore(eq(A_FILESTORE));
		verify(fileStore).folders();
		verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);

		assertThat(response.getElements(),
				containsInAnyOrder( //
						newFileSystemObject() //
								.withId(firstFolder.getName()) //
								.withName(firstFolder.getName()) //
								.withParent(firstFolder.getParent()) //
								.build(), //
						newFileSystemObject() //
								.withId(secondFolder.getName()) //
								.withName(secondFolder.getName()) //
								.withParent(secondFolder.getParent()) //
								.build(), //
						newFileSystemObject() //
								.withId(thirdFolder.getName()) //
								.withName(thirdFolder.getName()) //
								.withParent(thirdFolder.getParent()) //
								.build()));
		assertThat(response.getMetadata().getTotal(), equalTo(3L));
	}

	@Test(expected = DummyException.class)
	public void readFolderCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(fileLogic).fileStore(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFolder(A_FILESTORE, A_FOLDER);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(errorHandler).dataStoreNotFound(eq(A_FILESTORE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void readFolderCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(fileStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFolder(A_FILESTORE, A_FOLDER);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test
	public void readFolder() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());

		// when
		final ResponseSingle<FileSystemObject> response = underTest.readFolder(A_FILESTORE, A_FOLDER);

		// then
		verify(fileLogic).fileStore(eq(A_FILESTORE));
		verify(fileStore).folder(eq(A_FOLDER));
		verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);

		assertThat(response.getElement(),
				equalTo( //
						newFileSystemObject() //
								.withId(folder.getName()) //
								.withName(folder.getName()) //
								.withParent(folder.getParent()) //
								.build()));
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(fileLogic).fileStore(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());
		final File file = temporaryFolder.newFile();

		try {
			// when
			underTest.uploadFile(A_FILESTORE, A_FOLDER, new DataHandler(new FileDataSource(file)));
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(errorHandler).dataStoreNotFound(eq(A_FILESTORE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(fileStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());
		final File file = temporaryFolder.newFile();

		try {
			// when
			underTest.uploadFile(A_FILESTORE, A_FOLDER, new DataHandler(new FileDataSource(file)));
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void uploadFileCallsErrorHandlerWhenFileNameIsDuplicated() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		final File file = temporaryFolder.newFile();
		final Element alreadyCreatedFile = element(file);
		doReturn(asList(alreadyCreatedFile)) //
				.when(fileStore).files(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).duplicateFileName(anyString());

		try {
			// when
			underTest.uploadFile(A_FILESTORE, A_FOLDER, new DataHandler(new FileDataSource(file)));
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(fileStore).files(eq(A_FOLDER));
			verify(errorHandler).duplicateFileName(eq(alreadyCreatedFile.getName()));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	// TODO test error on creation

	@Test
	public void uploadFile() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		doReturn(asList()) //
				.when(fileStore).files(any(String.class));
		final Element created = element(temporaryFolder.newFile());
		doReturn(of(created)) //
				.when(fileStore).create(any(String.class), any(DataHandler.class));
		final File file = temporaryFolder.newFile();
		final DataHandler dataHandler = new DataHandler(new FileDataSource(file));

		// when
		underTest.uploadFile(A_FILESTORE, A_FOLDER, dataHandler);

		// then
		verify(fileLogic).fileStore(eq(A_FILESTORE));
		verify(fileStore).folder(eq(A_FOLDER));
		verify(fileStore).files(eq(A_FOLDER));
		verify(fileStore).create(eq(A_FOLDER), eq(dataHandler));
		verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
	}

	@Test(expected = DummyException.class)
	public void readFilesCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(fileLogic).fileStore(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFiles(A_FILESTORE, A_FOLDER);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(errorHandler).dataStoreNotFound(eq(A_FILESTORE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void readFilesCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(fileStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFiles(A_FILESTORE, A_FOLDER);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test
	public void readFiles() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(fileStore).files(any(String.class));

		// when
		final ResponseMultiple<FileSystemObject> response = underTest.readFiles(A_FILESTORE, A_FOLDER);

		// then
		verify(fileLogic).fileStore(eq(A_FILESTORE));
		verify(fileStore).folder(eq(A_FOLDER));
		verify(fileStore).files(eq(A_FOLDER));
		verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);

		assertThat(response.getElements(),
				containsInAnyOrder( //
						newFileSystemObject() //
								.withId(firstFile.getName()) //
								.withName(firstFile.getName()) //
								.withParent(firstFile.getParent()) //
								.build(), //
						newFileSystemObject() //
								.withId(secondFile.getName()) //
								.withName(secondFile.getName()) //
								.withParent(secondFile.getParent()) //
								.build(), //
						newFileSystemObject() //
								.withId(thirdFile.getName()) //
								.withName(thirdFile.getName()) //
								.withParent(thirdFile.getParent()) //
								.build()));
		assertThat(response.getMetadata().getTotal(), equalTo(3L));
	}

	@Test(expected = DummyException.class)
	public void readFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(fileLogic).fileStore(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.readFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(errorHandler).dataStoreNotFound(eq(A_FILESTORE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void readFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(fileStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.readFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void readFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(fileStore).files(any(String.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.readFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(fileStore).files(eq(A_FOLDER));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test
	public void readFile() throws Exception {
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile(A_FILE));
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(fileStore).files(any(String.class));

		// when
		final ResponseSingle<FileSystemObject> response = underTest.readFile(A_FILESTORE, A_FOLDER, A_FILE);

		// then
		verify(fileLogic).fileStore(eq(A_FILESTORE));
		verify(fileStore).folder(eq(A_FOLDER));
		verify(fileStore).files(eq(A_FOLDER));
		verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);

		assertThat(response.getElement(),
				equalTo( //
						newFileSystemObject() //
								.withId(firstFile.getName()) //
								.withName(firstFile.getName()) //
								.withParent(firstFile.getParent()) //
								.build()));
	}

	@Test(expected = DummyException.class)
	public void downloadFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(fileLogic).fileStore(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.downloadFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(errorHandler).dataStoreNotFound(eq(A_FILESTORE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void downalodFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(fileStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.downloadFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void downloadFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(fileStore).files(any(String.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.downloadFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(fileStore).files(eq(A_FOLDER));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test
	public void downloadFile() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile(A_FILE));
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(fileStore).files(any(String.class));
		final File file = temporaryFolder.newFile("the file name");
		write(get(file.toURI()), "foo bar baz".getBytes());
		doReturn(of(new DataHandler(new FileDataSource(file)))) //
				.when(fileStore).download(any(Element.class));

		// when
		final DataHandler response = underTest.downloadFile(A_FILESTORE, A_FOLDER, A_FILE);

		// then
		verify(fileLogic).fileStore(eq(A_FILESTORE));
		verify(fileStore).folder(eq(A_FOLDER));
		verify(fileStore).files(eq(A_FOLDER));
		verify(fileStore).download(eq(firstFile));
		verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);

		assertThat(response.getName(), equalTo("the file name"));
		assertThat(toString(response.getInputStream()), equalTo("foo bar baz"));
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenDataStoreIsNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(fileLogic).fileStore(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).dataStoreNotFound(anyString());

		try {
			// when
			underTest.deleteFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(errorHandler).dataStoreNotFound(eq(A_FILESTORE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenFolderIsNotFound() throws Exception {
		// given
		doReturn(empty()) //
				.when(fileStore).folder(anyString());
		doThrow(DummyException.class) //
				.when(errorHandler).folderNotFound(anyString());

		try {
			// when
			underTest.deleteFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(errorHandler).folderNotFound(eq(A_FOLDER));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test(expected = DummyException.class)
	public void deleteFileCallsErrorHandlerWhenFileIsNotFound() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		final Element firstFile = element(temporaryFolder.newFile());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(fileStore).files(any(String.class));
		doThrow(DummyException.class) //
				.when(errorHandler).fileNotFound(anyString());

		try {
			// when
			underTest.deleteFile(A_FILESTORE, A_FOLDER, A_FILE);
		} finally {
			// then
			verify(fileLogic).fileStore(eq(A_FILESTORE));
			verify(fileStore).folder(eq(A_FOLDER));
			verify(fileStore).files(eq(A_FOLDER));
			verify(errorHandler).fileNotFound(eq(A_FILE));
			verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
		}
	}

	@Test
	public void deleteFile() throws Exception {
		// given
		final Element folder = element(temporaryFolder.newFolder());
		doReturn(of(folder)) //
				.when(fileStore).folder(anyString());
		final File file = temporaryFolder.newFile(A_FILE);
		final Element firstFile = element(file);
		write(get(file.toURI()), "foo bar baz".getBytes());
		final Element secondFile = element(temporaryFolder.newFile());
		final Element thirdFile = element(temporaryFolder.newFile());
		doReturn(asList(firstFile, secondFile, thirdFile)) //
				.when(fileStore).files(any(String.class));

		// when
		underTest.deleteFile(A_FILESTORE, A_FOLDER, A_FILE);

		// then
		verify(fileLogic).fileStore(eq(A_FILESTORE));
		verify(fileStore).folder(eq(A_FOLDER));
		verify(fileStore).files(eq(A_FOLDER));
		verify(fileStore).delete(eq(firstFile));
		verifyNoMoreInteractions(errorHandler, fileLogic, fileStore);
	}

	private static Element element(final File file) {
		return new Element() {

			@Override
			public String getId() {
				return file.getName();
			}

			@Override
			public String getParent() {
				return file.getParent();
			}

			@Override
			public String getName() {
				return file.getName();
			}

			@Override
			public String getPath() {
				return file.getPath();
			}

		};
	}

	private static String toString(final InputStream is) {
		final Scanner scanner = new Scanner(is).useDelimiter("\\A");
		try {
			return scanner.hasNext() ? scanner.next() : null;
		} finally {
			scanner.close();
		}
	}

}
