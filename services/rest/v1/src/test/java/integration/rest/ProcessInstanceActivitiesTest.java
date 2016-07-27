package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v1.model.Models.newAttributeStatus;
import static org.cmdbuild.service.rest.v1.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessActivityWithBasicDetails;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessActivityWithFullDetails;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
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
import org.cmdbuild.service.rest.v1.ProcessInstanceActivities;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.v1.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ProcessInstanceActivitiesTest {

	@ClassRule
	public static ServerResource<ProcessInstanceActivities> server = ServerResource
			.newInstance(ProcessInstanceActivities.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private ProcessInstanceActivities service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(ProcessInstanceActivities.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void instancesRead() throws Exception {
		// given
		final ResponseMultiple<ProcessActivityWithBasicDetails> sentResponse = newResponseMultiple(
				ProcessActivityWithBasicDetails.class) //
						.withElements(asList( //
								newProcessActivityWithBasicDetails() //
										.withId("123") //
										.withWritableStatus(true) //
										.build(), //
								newProcessActivityWithBasicDetails() //
										.withId("456") //
										.withWritableStatus(false) //
										.build() //
		)) //
						.withMetadata(newMetadata() //
								.withTotal(2L) //
								.build()) //
						.build();
		final ResponseMultiple<ProcessActivityWithBasicDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong());

		// when
		final HttpGet get = new HttpGet(server.resource("processes/123/instances/456/activities"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("123"), eq(456L));
	}

	@Test
	public void instanceRead() throws Exception {
		// given
		final ResponseSingle<ProcessActivityWithFullDetails> sentResponse = newResponseSingle(
				ProcessActivityWithFullDetails.class) //
						.withElement(newProcessActivityWithFullDetails() //
								.withId("123") //
								.withDescription("description") //
								.withInstructions("instructions") //
								.withAttributes(asList( //
										newAttributeStatus() //
												.withId("456") //
												.withWritable(true) //
												.withMandatory(false) //
												.withIndex(0L) //
												.build(), //
										newAttributeStatus() //
												.withId("789") //
												.withMandatory(true) //
												.withIndex(1L) //
												.build() //
		)) //
								.build()) //
						.build();
		final ResponseSingle<ProcessActivityWithFullDetails> expectedResponse = sentResponse;
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong(), anyString());

		// when
		final HttpGet get = new HttpGet(server.resource("processes/123/instances/456/activities/789/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("123"), eq(456L), eq("789"));
	}

}
