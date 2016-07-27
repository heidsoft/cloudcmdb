package integration.rest;

import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v1.Impersonate;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ImpersonateTest {

	@ClassRule
	public static ServerResource<Impersonate> server = ServerResource.newInstance(Impersonate.class) //
			.withPortRange(randomPort()) //
			.build();

	private Impersonate service;
	private HttpClient httpclient;

	@Before
	public void createHttpClient() throws Exception {
		server.service(service = mock(Impersonate.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void start() throws Exception {
		// when
		final HttpPut put = new HttpPut(server.resource("sessions/foo/impersonate/bar/"));
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		verify(service).start(eq("foo"), eq("bar"));
	}

	@Test
	public void stop() throws Exception {
		// when
		final HttpDelete delete = new HttpDelete(server.resource("sessions/foo/impersonate/"));
		final HttpResponse response = httpclient.execute(delete);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		verify(service).stop(eq("foo"));
	}

}
