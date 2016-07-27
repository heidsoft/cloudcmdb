package integration.rest;

import static java.util.Arrays.asList;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.v2.model.Models.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.Sessions;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Session;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SessionsTest {

	@ClassRule
	public static ServerResource<Sessions> server = ServerResource.newInstance(Sessions.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Sessions service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Sessions.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void created() throws Exception {
		// given
		final ResponseSingle<Session> sentResponse = newResponseSingle(Session.class) //
				.withElement(newSession() //
						.withId("token") //
						.withUsername("username") //
						.withPassword("password") //
						.withRole("role") //
						.withAvailableRoles(asList("foo", "bar", "baz")) //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).create(any(Session.class));

		// when
		final HttpPost post = new HttpPost(server.resource("sessions/"));
		post.setEntity(new StringEntity( //
				"{\"username\" : \"foo\", \"password\" : \"bar\"}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(sentResponse)));

		final ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
		verify(service).create(captor.capture());

		final Session captured = captor.getValue();
		assertThat(captured.getUsername(), equalTo("foo"));
		assertThat(captured.getPassword(), equalTo("bar"));
	}

	@Test
	public void readed() throws Exception {
		// given
		final ResponseSingle<Session> sentResponse = newResponseSingle(Session.class) //
				.withElement(newSession() //
						.withId("the id") //
						.withUsername("the username") //
						.withPassword("the password") //
						.withRole("the role") //
						.withAvailableRoles(asList("foo", "bar", "baz")) //
						.build() //
		) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString());

		// when
		final HttpGet get = new HttpGet(server.resource("sessions/foo/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(sentResponse)));

		verify(service).read(eq("foo"));
	}

	@Test
	public void updated() throws Exception {
		// given
		final ResponseSingle<Session> sentResponse = newResponseSingle(Session.class) //
				.withElement(newSession() //
						.withId("token") //
						.withUsername("username") //
						.withPassword("password") //
						.withRole("role") //
						.withAvailableRoles(asList("foo", "bar", "baz")) //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).update(anyString(), any(Session.class));

		// when
		final HttpPut put = new HttpPut(server.resource("sessions/foo/"));
		put.setEntity(new StringEntity( //
				"{\"_id\" : \"ignored\", \"username\" : \"bar\", \"password\" : null, \"role\" : \"baz\"}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(sentResponse)));

		final ArgumentCaptor<Session> captor = ArgumentCaptor.forClass(Session.class);
		verify(service).update(eq("foo"), captor.capture());

		final Session captured = captor.getValue();
		assertThat(captured.getUsername(), equalTo("bar"));
		assertThat(captured.getRole(), equalTo("baz"));
	}

	@Test
	public void deleted() throws Exception {
		// when
		final HttpDelete delete = new HttpDelete(server.resource("sessions/foo/"));
		final HttpResponse response = httpclient.execute(delete);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		verify(service).delete(eq("foo"));
	}

}
