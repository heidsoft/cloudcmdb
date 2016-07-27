package integration.rest;

import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.model.Models.newGraphConfiguration;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
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
import org.cmdbuild.service.rest.v2.GraphConfiguration;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class GraphConfigurationTest {

	@ClassRule
	public static ServerResource<GraphConfiguration> server = ServerResource.newInstance(GraphConfiguration.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private GraphConfiguration service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(GraphConfiguration.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void read() throws Exception {
		// given
		final ResponseSingle<org.cmdbuild.service.rest.v2.model.GraphConfiguration> expectedResponse = newResponseSingle(
				org.cmdbuild.service.rest.v2.model.GraphConfiguration.class) //
						.withElement(newGraphConfiguration() //
								.withEnabledStatus(true) //
								.withBaseLevel(1) //
								.withClusteringThreshold(2) //
								.withDisplayLabel("this is the label") //
								.withEdgeColor("this is the color") //
								.withEdgeTooltipEnabled(true) //
								.withNodeTooltipEnabled(true) //
								.withSpriteDimension(3) //
								.withStepRadius(4) //
								.withViewPointDistance(5) //
								.withViewPointHeight(6) //
								.build()) //
						.withMetadata(newMetadata() //
								// nothing to add, just needed for simplify
								// assertions
								.build()) //
						.build();
		when(service.read()) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("configuration/graph/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));
		verify(service).read();
	}

}
