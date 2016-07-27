package integration.rest;

import static java.util.Arrays.asList;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.model.Models.newIcon;
import static org.cmdbuild.service.rest.v2.model.Models.newImage;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.Icons;
import org.cmdbuild.service.rest.v2.model.Icon;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class IconsTest {

	@ClassRule
	public static ServerResource<Icons> server = ServerResource.newInstance(Icons.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Icons service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Icons.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void create() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(42L) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(any(Icon.class));

		// when
		final HttpPost request = new HttpPost(server.resource("icons/"));
		request.setEntity(new StringEntity(
				"{" //
						+ "	\"type\": \"the icon type\"," //
						+ "	\"details\": {" //
						+ "		\"foo\": \"Foo\"," //
						+ "		\"bar\": \"Bar\"" //
						+ "	}," //
						+ "	\"image\": {" //
						+ "		\"type\": \"the image type\"," //
						+ "		\"details\": {" //
						+ "			\"baz\": \"Baz\"" //
						+ "		}" //
						+ "	}" //
						+ "}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(request);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<Icon> captor = ArgumentCaptor.forClass(Icon.class);
		verify(service).create(captor.capture());
		verifyNoMoreInteractions(service);

		assertThat(captor.getValue(),
				equalTo(newIcon() //
						.withId(null) //
						.withType("the icon type") //
						.withDetail("foo", "Foo") //
						.withDetail("bar", "Bar") //
						.withImage(newImage() //
								.withType("the image type") //
								.withDetail("baz", "Baz") //
								.build()) //
				.build()));
	}

	@Test
	public void readAll() throws Exception {
		// given
		final ResponseMultiple<Icon> expectedResponse = newResponseMultiple(Icon.class) //
				.withElements(asList( //
						newIcon() //
								.withId(1L) //
								.withType("the type") //
								.withDetail("foo", "Foo") //
								.withImage(newImage() //
										.withType("the image type") //
										.withDetail("lol", "LOL") //
										.build()) //
								.build(),
						newIcon() //
								.withId(2L) //
								.withType("another type") //
								.withDetail("bar", "Bar") //
								.withImage(newImage() //
										.withType("another image type") //
										.withDetail("rotfl", "ROTFL") //
										.build()) //
								.build(),
						newIcon() //
								.withId(3L) //
								.withType("yet another type") //
								.withDetail("baz", "Baz") //
								.withImage(newImage() //
										.withType("yet another image type") //
										.build()) //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.read()) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet request = new HttpGet(new URIBuilder(server.resource("icons/")) //
				.build());
		final HttpResponse response = httpclient.execute(request);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read();
	}

	@Test
	public void read() throws Exception {
		// given
		final ResponseSingle<Icon> expectedResponse = newResponseSingle(Icon.class) //
				.withElement(newIcon() //
						.withId(42L) //
						.withType("the type") //
						.withDetail("foo", "Foo") //
						.withDetail("bar", "Bar") //
						.withImage(newImage() //
								.withType("the image type") //
								.withDetail("baz", "Baz") //
								.build()) //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.read(anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet request = new HttpGet(new URIBuilder(server.resource("icons/42/")) //
				.build());
		final HttpResponse response = httpclient.execute(request);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq(42L));
	}

	@Test
	public void update() throws Exception {
		// when
		final HttpPut request = new HttpPut(server.resource("icons/42/"));
		request.setEntity(new StringEntity(
				"{" //
						+ "	\"_id\": 42," //
						+ "	\"type\": \"the icon type\"," //
						+ "	\"details\": {" //
						+ "		\"foo\": \"Foo\"," //
						+ "		\"bar\": \"Bar\"" //
						+ "	}," //
						+ "	\"image\": {" //
						+ "		\"type\": \"the image type\"," //
						+ "		\"details\": {" //
						+ "			\"baz\": \"Baz\"" //
						+ "		}" //
						+ "	}" //
						+ "}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(request);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<Icon> captor = ArgumentCaptor.forClass(Icon.class);
		verify(service).update(eq(42L), captor.capture());
		verifyNoMoreInteractions(service);

		assertThat(captor.getValue(),
				equalTo(newIcon() //
						.withId(42L) //
						.withType("the icon type") //
						.withDetail("foo", "Foo") //
						.withDetail("bar", "Bar") //
						.withImage(newImage() //
								.withType("the image type") //
								.withDetail("baz", "Baz") //
								.build()) //
				.build()));
	}

	@Test
	public void delete() throws Exception {
		// when
		final HttpDelete request = new HttpDelete(server.resource("icons/42/"));
		final HttpResponse response = httpclient.execute(request);

		// then
		verify(service).delete(eq(42L));
		verifyNoMoreInteractions(service);

		assertThat(statusCodeOf(response), equalTo(204));
	}

}
