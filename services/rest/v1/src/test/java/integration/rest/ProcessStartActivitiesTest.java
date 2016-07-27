package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v1.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessActivityWithBasicDetails;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v1.ProcessStartActivities;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ProcessStartActivitiesTest {

	@ClassRule
	public static ServerResource<ProcessStartActivities> server = ServerResource
			.newInstance(ProcessStartActivities.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private ProcessStartActivities service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(ProcessStartActivities.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void allStartActivitiesRead() throws Exception {
		// given
		final ProcessActivityWithBasicDetails firstActivity = newProcessActivityWithBasicDetails() //
				.withId("foo") //
				.withDescription("foo foo foo") //
				.build();
		final ProcessActivityWithBasicDetails secondActivity = newProcessActivityWithBasicDetails() //
				.withId("bar") //
				.withDescription("bar bar bar") //
				.build();
		final ResponseMultiple<ProcessActivityWithBasicDetails> sentResponse = newResponseMultiple(
				ProcessActivityWithBasicDetails.class) //
						.withElements(asList(firstActivity, secondActivity)) //
						.withMetadata(newMetadata() //
								.withTotal(3L) //
								.build()) //
						.build();
		final ResponseMultiple<ProcessActivityWithBasicDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read(anyString());

		// when
		final HttpGet get = new HttpGet(server.resource("processes/baz/start_activities/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("baz"));
	}

	@Test
	public void startActivityDetailRead() throws Exception {
		// given
		final ProcessActivityWithFullDetails firstActivity = newProcessActivityWithFullDetails() //
				.withId("foo") //
				.withDescription("foo foo foo") //
				.withInstructions("blah blah blah") //
				.build();
		final ResponseSingle<ProcessActivityWithFullDetails> sentResponse = newResponseSingle(
				ProcessActivityWithFullDetails.class) //
						.withElement(firstActivity) //
						.build();
		final ResponseSingle<ProcessActivityWithFullDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyString());

		// when
		final HttpGet get = new HttpGet(server.resource("processes/bar/start_activities/baz/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("bar"), eq("baz"));
	}

}
