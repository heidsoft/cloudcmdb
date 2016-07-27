package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newProcessWithBasicDetails;
import static org.cmdbuild.service.rest.v2.model.Models.newProcessWithFullDetails;
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
import org.cmdbuild.service.rest.v2.Processes;
import org.cmdbuild.service.rest.v2.model.ProcessWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.ProcessWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ProcessesTest {

	@ClassRule
	public static ServerResource<Processes> server = ServerResource.newInstance(Processes.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Processes service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Processes.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void getProcesses() throws Exception {
		// given
		final ResponseMultiple<ProcessWithBasicDetails> expectedResponse = newResponseMultiple(
				ProcessWithBasicDetails.class) //
						.withElements(asList( //
								newProcessWithBasicDetails() //
										.withName("foo") //
										.build(), //
								newProcessWithBasicDetails() //
										.withName("bar") //
										.build())) //
						.withMetadata(newMetadata() //
								.withTotal(2L) //
								.build()) //
						.build();
		when(service.readAll(anyBoolean(), anyInt(), anyInt())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("processes/")) //
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
	public void getProcessDetail() throws Exception {
		// given
		final ResponseSingle<ProcessWithFullDetails> expectedResponse = newResponseSingle(ProcessWithFullDetails.class) //
				.withElement(newProcessWithFullDetails() //
						.withName("foo") //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("processes/dummy/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read("dummy");
	}

	@Test
	public void generateId() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(42L) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.generateId(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("processes/dummy/generate_id"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).generateId("dummy");
	}

}
