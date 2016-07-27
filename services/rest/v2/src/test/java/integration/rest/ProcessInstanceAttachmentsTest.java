package integration.rest;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.http.Consts.UTF_8;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ATTACHMENT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILE;
import static org.cmdbuild.service.rest.v2.model.Models.newAttachment;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.v2.model.Models.newValues;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.ProcessInstanceAttachments;
import org.cmdbuild.service.rest.v2.model.Attachment;
import org.cmdbuild.service.rest.v2.model.Models;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Values;
import org.cmdbuild.service.rest.v2.model.adapter.AttachmentAdapter;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class ProcessInstanceAttachmentsTest {

	@ClassRule
	public static ServerResource<ProcessInstanceAttachments> server = ServerResource
			.newInstance(ProcessInstanceAttachments.class) //
			.withPortRange(randomPort()) //
			.build();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static JsonSupport json = new JsonSupport();

	private ProcessInstanceAttachments service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(ProcessInstanceAttachments.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void createWithBothAttachmentAndFile() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");
		final ResponseSingle<String> expectedResponse = newResponseSingle(String.class) //
				.withElement("the id") //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), anyLong(), any(Attachment.class), any(DataHandler.class));

		// when
		final HttpPost post = new HttpPost(server.resource("processes/foo/instances/123/attachments/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(ATTACHMENT,
				new StringBody("{" //
						+ "\"_description\": \"the description\"" //
						+ "}", APPLICATION_JSON, UTF_8));
		multipartEntity.addPart(FILE, new FileBody(file));
		post.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).create(eq("foo"), eq(123L), attachmentCaptor.capture(), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment.getDescription(), equalTo("the description"));

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(dataHandler.getInputStream()), equalTo(toString(new FileInputStream(file))));
	}

	@Test
	@Ignore("data handler seems correct but it looks empty even if it's referring to the right file")
	public void createWithFileOnly() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");
		final ResponseSingle<String> expectedResponse = newResponseSingle(String.class) //
				.withElement("the id") //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), anyLong(), any(Attachment.class), any(DataHandler.class));

		// when
		final HttpPost post = new HttpPost(server.resource("processes/foo/instances/123/attachments/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(FILE, new FileBody(file));
		post.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).create(eq("foo"), eq(123L), isNull(Attachment.class), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(dataHandler.getInputStream()), equalTo(toString(new FileInputStream(file))));
	}

	@Test
	public void readAll() throws Exception {
		// given
		final Attachment foo = newAttachment() //
				.withId("foo") //
				.withDescription("this is foo") //
				.build();
		final Attachment bar = newAttachment() //
				.withId("bar") //
				.withDescription("this is bar") //
				.build();
		final Attachment baz = newAttachment() //
				.withId("baz") //
				.withDescription("this is baz") //
				.build();
		final ResponseMultiple<Attachment> sentResponse = newResponseMultiple(Attachment.class) //
				.withElements(asList(foo, bar, baz)) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		final AttachmentAdapter adapter = new AttachmentAdapter();
		final ResponseMultiple<Values> expectedResponse = newResponseMultiple(Values.class) //
				.withElements(asList(adapter.marshal(foo), adapter.marshal(bar), adapter.marshal(baz))) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.read(anyString(), anyLong())) //
				.thenReturn(sentResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("processes/dummy/instances/123/attachments/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		verify(service).read(eq("dummy"), eq(123L));
		verifyNoMoreInteractions(service);

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void read() throws Exception {
		// given
		final Attachment attachmentMetadata = newAttachment() //
				.withId("baz") //
				.withName("my name is Baz") //
				.withCategory("something") //
				.withDescription("nice to meet you") //
				.withMetadata(newValues() //
						.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut("first", 1) //
								.chainablePut("second", "2"))//
						.build()) //
				.build();
		final ResponseSingle<Attachment> sentResponse = newResponseSingle(Attachment.class) //
				.withElement(attachmentMetadata) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		final ResponseSingle<Map<String, Object>> expectedResponse = Models.<Map<String, Object>> newResponseSingle() //
				.withElement(new AttachmentAdapter().marshal(attachmentMetadata)) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.read(anyString(), anyLong(), anyString())) //
				.thenReturn(sentResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("processes/foo/instances/123/attachments/bar/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		verify(service).read(eq("foo"), eq(123L), eq("bar"));
		verifyNoMoreInteractions(service);

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
	}

	@Test
	public void download() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		final DataHandler expectedResponse = new DataHandler(file.toURI().toURL());
		when(service.download(anyString(), anyLong(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("processes/dummy/instances/123/attachments/foo/file/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		verify(service).download(eq("dummy"), eq(123L), eq("foo"));
		verifyNoMoreInteractions(service);

		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(toByteArray(contentOf(response)), equalTo(toByteArray(expectedResponse.getInputStream())));
	}

	@Test
	public void updateWithBothAttachmentAndFile() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final HttpPut put = new HttpPut(server.resource("processes/foo/instances/123/attachments/bar/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(ATTACHMENT,
				new StringBody("{" //
						+ "\"_description\": \"the new description\"" //
						+ "}", APPLICATION_JSON, UTF_8));
		multipartEntity.addPart(FILE, new FileBody(file));
		put.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).update(eq("foo"), eq(123L), eq("bar"), attachmentCaptor.capture(), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment.getDescription(), equalTo("the new description"));

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(new FileInputStream(file)), equalTo(toString(dataHandler.getInputStream())));
	}

	@Test
	@Ignore("data handler seems correct but it looks empty even if it's referring to the right file")
	public void updateWithFileOnly() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final HttpPut put = new HttpPut(server.resource("processes/foo/instances/123/attachments/bar/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(FILE, new FileBody(file));
		put.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<DataHandler> dataHandlerCaptor = ArgumentCaptor.forClass(DataHandler.class);
		verify(service).update(eq("foo"), eq(123L), eq("bar"), isNull(Attachment.class), dataHandlerCaptor.capture());
		verifyNoMoreInteractions(service);

		final DataHandler dataHandler = dataHandlerCaptor.getValue();
		assertThat(toString(new FileInputStream(file)), equalTo(toString(dataHandler.getInputStream())));
	}

	@Test
	public void updateWithAttachmentOnly() throws Exception {
		// given
		final File file = temporaryFolder.newFile();
		write(file, "blah blah blah");

		// when
		final HttpPut put = new HttpPut(server.resource("processes/foo/instances/123/attachments/bar/"));
		final MultipartEntity multipartEntity = new MultipartEntity();
		multipartEntity.addPart(ATTACHMENT,
				new StringBody("{" //
						+ "\"_description\": \"the new description\"" //
						+ "}", APPLICATION_JSON, UTF_8));
		put.setEntity(multipartEntity);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<Attachment> attachmentCaptor = ArgumentCaptor.forClass(Attachment.class);
		verify(service).update(eq("foo"), eq(123L), eq("bar"), attachmentCaptor.capture(), isNull(DataHandler.class));
		verifyNoMoreInteractions(service);

		final Attachment attachment = attachmentCaptor.getValue();
		assertThat(attachment.getDescription(), equalTo("the new description"));
	}

	@Test
	public void delete() throws Exception {
		// when
		final HttpDelete delete = new HttpDelete(server.resource("processes/dummy/instances/123/attachments/foo/"));
		final HttpResponse response = httpclient.execute(delete);

		// then
		verify(service).delete(eq("dummy"), eq(123L), eq("foo"));
		verifyNoMoreInteractions(service);

		assertThat(statusCodeOf(response), equalTo(204));
	}

	private static String toString(final InputStream inputStream) throws IOException {
		final String string = IOUtils.toString(inputStream);
		IOUtils.closeQuietly(inputStream);
		return string;
	}

}
