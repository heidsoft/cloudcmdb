package integration.rest;

import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILE;
import static org.cmdbuild.service.rest.v2.model.Models.newFileSystemObject;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.FileStores;
import org.cmdbuild.service.rest.v2.model.FileSystemObject;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class FileStoresTest {

	@ClassRule
	public static ServerResource<FileStores> server = ServerResource.newInstance(FileStores.class) //
			.withPortRange(randomPort()) //
			.build();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static JsonSupport json = new JsonSupport();

	private FileStores service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(FileStores.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void readFolders() throws Exception {
		// given
		final ResponseMultiple<FileSystemObject> expectedResponse = newResponseMultiple(FileSystemObject.class) //
				.withElement(newFileSystemObject() //
						.withId("the_id") //
						.withName("the name") //
						.withParent("the parent") //
						.build()) //
				.withElement(newFileSystemObject() //
						.withId("another_id") //
						.withName("another name") //
						.withParent("another parent") //
						.build()) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.readFolders(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet request = new HttpGet(new URIBuilder(server.resource("filestores/the_filestore/folders/")) //
				.build());
		final HttpResponse response = httpclient.execute(request);

		// then
		verify(service).readFolders(eq("the_filestore"));

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void readFolder() throws Exception {
		// given
		final ResponseSingle<FileSystemObject> expectedResponse = newResponseSingle(FileSystemObject.class) //
				.withElement(newFileSystemObject() //
						.withId("the_id") //
						.withName("the name") //
						.withParent("the parent") //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.readFolder(anyString(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet request = new HttpGet(
				new URIBuilder(server.resource("filestores/the_filestore/folders/the_folder/")) //
						.build());
		final HttpResponse response = httpclient.execute(request);

		// then
		verify(service).readFolder(eq("the_filestore"), eq("the_folder"));

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
	}

	@Test
	@Ignore("captured DataHandler has an InputStream that seems alread read")
	public void uploadFile() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(get(file.toURI()), "foo bar baz".getBytes());
		final ResponseSingle<FileSystemObject> expectedResponse = newResponseSingle(FileSystemObject.class) //
				.withElement(newFileSystemObject() //
						.withId("the file") //
						.withName("the name") //
						.withParent("the parent") //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).uploadFile(anyString(), anyString(), any(DataHandler.class));

		// when
		final HttpPost request = new HttpPost(
				new URIBuilder(server.resource("filestores/the_filestore/folders/the_folder/files/")) //
						.build());
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(FILE, new FileBody(file));
		request.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(request);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).uploadFile(eq("the_filestore"), eq("the_folder"), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(dataHandler.getInputStream()), equalTo("foo bar baz"));
	}

	@Test
	public void readFiles() throws Exception {
		// given
		final ResponseMultiple<FileSystemObject> expectedResponse = newResponseMultiple(FileSystemObject.class) //
				.withElement(newFileSystemObject() //
						.withId("the_id") //
						.withName("the name") //
						.withParent("the parent") //
						.build()) //
				.withElement(newFileSystemObject() //
						.withId("another_id") //
						.withName("another name") //
						.withParent("another parent") //
						.build()) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.readFiles(anyString(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet request = new HttpGet(
				new URIBuilder(server.resource("filestores/the_filestore/folders/the_folder/files/")) //
						.build());
		final HttpResponse response = httpclient.execute(request);

		// then
		verify(service).readFiles(eq("the_filestore"), eq("the_folder"));

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void readFile() throws Exception {
		// given
		final ResponseSingle<FileSystemObject> expectedResponse = newResponseSingle(FileSystemObject.class) //
				.withElement(newFileSystemObject() //
						.withId("the_id") //
						.withName("the name") //
						.withParent("the parent") //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.readFile(anyString(), anyString(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet request = new HttpGet(
				new URIBuilder(server.resource("filestores/the_filestore/folders/the_folder/files/the_file/")) //
						.build());
		final HttpResponse response = httpclient.execute(request);

		// then
		verify(service).readFile(eq("the_filestore"), eq("the_folder"), eq("the_file"));

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void downloadFile() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(get(file.toURI()), "foo bar baz".getBytes());
		final DataHandler expectedResponse = new DataHandler(new FileDataSource(file));
		when(service.downloadFile(anyString(), anyString(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet request = new HttpGet(
				new URIBuilder(server.resource("filestores/the_filestore/folders/the_folder/files/the_file/download")) //
						.build());
		final HttpResponse response = httpclient.execute(request);

		// then
		verify(service).downloadFile(eq("the_filestore"), eq("the_folder"), eq("the_file"));
		verifyNoMoreInteractions(service);

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(toString(contentOf(response)), equalTo("foo bar baz"));
	}

	@Test
	public void deleteFile() throws Exception {
		// when
		final HttpDelete request = new HttpDelete(
				new URIBuilder(server.resource("filestores/the_filestore/folders/the_folder/files/the_file/")) //
						.build());
		final HttpResponse response = httpclient.execute(request);

		// then
		verify(service).deleteFile(eq("the_filestore"), eq("the_folder"), eq("the_file"));

		assertThat(statusCodeOf(response), equalTo(204));
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
