package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newAttributeOrder;
import static org.cmdbuild.service.rest.v2.model.Models.newClassWithBasicDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newClassWithFullDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.Classes;
import org.cmdbuild.service.rest.v2.model.ClassWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.ClassWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClassesTest {

	@ClassRule
	public static ServerResource<Classes> server = ServerResource.newInstance(Classes.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Classes service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Classes.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void getClasses() throws Exception {
		// given
		final ResponseMultiple<ClassWithBasicDetails> expectedResponse = newResponseMultiple(
				ClassWithBasicDetails.class) //
						.withElements(asList( //
								newClassWithBasicDetails() //
										.withName("foo") //
										.build(), //
								newClassWithBasicDetails() //
										.withName("bar") //
										.build())) //
						.withMetadata(newMetadata() //
								.withTotal(2L) //
								.build()) //
						.build();
		when(service.readAll(anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("classes/")) //
				.setParameter(ACTIVE, "true") //
				.setParameter(LIMIT, "123") //
				.setParameter(START, "456") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readAll(eq(true), eq(123), eq(456));
	}

	@Test
	public void getClassDetail() throws Exception {
		// given
		final ResponseSingle<ClassWithFullDetails> expectedResponse = newResponseSingle(ClassWithFullDetails.class) //
				.withElement(newClassWithFullDetails() //
						.withName("foo") //
						.withDefaultOrder(asList( //
								newAttributeOrder() //
										.withAttribute("bar") //
										.withDirection("ascending") //
										.build(), //
								newAttributeOrder() //
										.withAttribute("baz") //
										.withDirection("descending") //
										.build() //
		)) //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("classes/123/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read("123");
	}
}
