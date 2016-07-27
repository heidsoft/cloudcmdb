package integration.rest;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PARAMETERS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newAttribute;
import static org.cmdbuild.service.rest.v2.model.Models.newFunctionWithBasicDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newFunctionWithFullDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.v2.model.Models.newValues;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.Functions;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.FunctionWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.FunctionWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Values;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class FunctionsTest {

	@ClassRule
	public static ServerResource<Functions> server = ServerResource.newInstance(Functions.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Functions service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Functions.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void functionsRetrieved() throws Exception {
		// given
		final ResponseMultiple<FunctionWithBasicDetails> expectedResponse = newResponseMultiple(
				FunctionWithBasicDetails.class) //
						.withElements(asList( //
								newFunctionWithBasicDetails() //
										.withId(1L) //
										.withName("foo") //
										.withDescription("Foo") //
										.build(), //
								newFunctionWithBasicDetails() //
										.withId(2L) //
										.withName("bar") //
										.withDescription("Bar") //
										.build())) //
						.withMetadata(newMetadata() //
								.withTotal(2L) //
								.build()) //
						.build();
		when(service.readAll(anyInt(), anyInt(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("functions/")) //
				.setParameter(LIMIT, "123") //
				.setParameter(START, "456") //
				.setParameter(FILTER, "this is the filter") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readAll(eq(123), eq(456), eq("this is the filter"));
	}

	@Test
	public void functionDetailsRetrieved() throws Exception {
		// given
		final ResponseSingle<FunctionWithFullDetails> expectedResponse = newResponseSingle(
				FunctionWithFullDetails.class) //
						.withElement(newFunctionWithFullDetails() //
								.withId(1L) //
								.withName("foo") //
								.withDescription("Foo") //
								.build()) //
						.withMetadata(newMetadata() //
								// nothing to add, just needed for simplify
								// assertions
								.build()) //
						.build();
		when(service.read(anyLong())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("functions/1/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq(1L));
	}

	@Test
	public void functionParametersRetrieved() throws Exception {
		// given
		final ResponseMultiple<Attribute> expectedResponse = newResponseMultiple(Attribute.class) //
				.withElements(asList( //
						newAttribute() //
								.withName("bar") //
								.build(), //
						newAttribute() //
								.withName("baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readInputParameters(anyLong(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("functions/1/parameters/")) //
				.setParameter(LIMIT, "456") //
				.setParameter(START, "789") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readInputParameters(eq(1L), eq(456), eq(789));
	}

	@Test
	public void functionAttributesRetrieved() throws Exception {
		// given
		final ResponseMultiple<Attribute> expectedResponse = newResponseMultiple(Attribute.class) //
				.withElements(asList( //
						newAttribute() //
								.withName("bar") //
								.build(), //
						newAttribute() //
								.withName("baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readOutputParameters(anyLong(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("functions/1/attributes/")) //
				.setParameter(LIMIT, "456") //
				.setParameter(START, "789") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readOutputParameters(eq(1L), eq(456), eq(789));
	}

	@Test
	public void functionOutputRetrieved() throws Exception {
		// given
		final Map<String, Object> values = newHashMap();
		final ResponseMultiple<Values> expectedResponse = newResponseMultiple(Values.class) //
				.withElements(asList( //
						newValues() //
								.withValues(values) //
								.build(), //
						newValues() //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.call(anyLong(), anyString())) //
				.thenReturn(expectedResponse);
		final String inputs = "" //
				+ "{" //
				+ "	\"foo\": \"FOO\"," //
				+ "	\"bar\": 1," //
				+ "	\"baz\"=true" //
				+ "}";

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("functions/1/outputs/")) //
				.setParameter(PARAMETERS, inputs) //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).call(eq(1L), eq(inputs));
	}

}
