package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v1.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessStatus;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseMultiple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v1.ProcessesConfiguration;
import org.cmdbuild.service.rest.v1.model.ProcessStatus;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ProcessConfigurationTest {

	@ClassRule
	public static ServerResource<ProcessesConfiguration> server = ServerResource
			.newInstance(ProcessesConfiguration.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private ProcessesConfiguration service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(ProcessesConfiguration.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void readStatuses() throws Exception {
		// given
		final ResponseMultiple<ProcessStatus> expectedResponse = newResponseMultiple(ProcessStatus.class) //
				.withElements(asList( //
						newProcessStatus() //
								.withId(123L) //
								.withValue("bar") //
								.withDescription("this is bar") //
								.build(), //
						newProcessStatus() //
								.withId(456L) //
								.withValue("baz") //
								.withDescription("this is baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.readStatuses()) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("configuration/processes/statuses/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).readStatuses();
	}

}
