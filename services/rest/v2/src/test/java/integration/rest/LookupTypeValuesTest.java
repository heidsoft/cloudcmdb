package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.model.Models.newLookupDetail;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.LookupTypeValues;
import org.cmdbuild.service.rest.v2.model.LookupDetail;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class LookupTypeValuesTest {

	@ClassRule
	public static ServerResource<LookupTypeValues> server = ServerResource.newInstance(LookupTypeValues.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private LookupTypeValues service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(LookupTypeValues.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void getLookups() throws Exception {
		// given
		final ResponseMultiple<LookupDetail> expectedResponse = newResponseMultiple(LookupDetail.class) //
				.withElements(asList( //
						newLookupDetail() //
								.withId(34L) //
								.withCode("foo") //
								.build(), //
						newLookupDetail() //
								.withId(56L) //
								.withCode("bar") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readAll(anyString(), anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("lookup_types/12/values/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readAll(eq("12"), eq(false), eq((Integer) null), eq((Integer) null));
	}

	@Test
	public void getLookup() throws Exception {
		// given
		final ResponseSingle<LookupDetail> expectedResponse = newResponseSingle(LookupDetail.class) //
				.withElement(newLookupDetail() //
						.withType("12") //
						.withId(34L) //
						.withCode("code") //
						.withDescription("description") //
						.withNumber(56L) //
						.withParentType("parent_type") //
						.withParentId(78L) //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.read(anyString(), anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("lookup_types/12/values/34/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("12"), eq(34L));
	}

}
