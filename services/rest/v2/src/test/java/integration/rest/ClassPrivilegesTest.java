package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.model.Models.newClassPrivilege;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
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
import org.cmdbuild.service.rest.v2.ClassPrivileges;
import org.cmdbuild.service.rest.v2.model.ClassPrivilege;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ClassPrivilegesTest {

	@ClassRule
	public static ServerResource<ClassPrivileges> server = ServerResource.newInstance(ClassPrivileges.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private ClassPrivileges service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(ClassPrivileges.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void readAll() throws Exception {
		// given
		final ResponseMultiple<ClassPrivilege> expectedResponse = newResponseMultiple(ClassPrivilege.class) //
				.withElements(asList( //
						newClassPrivilege() //
								.withId("bar") //
								.withName("Bar") //
								.withDescription("This is bar") //
								.withMode("mode") //
								.build(), //
						newClassPrivilege() //
								.withId("baz") //
								.withName("Baz") //
								.withDescription("This is baz") //
								.withMode("another mode") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		when(service.read(anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("roles/foo/classes_privileges/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("foo"));
	}

	@Test
	public void read() throws Exception {
		// given
		final ResponseSingle<ClassPrivilege> expectedResponse = newResponseSingle(ClassPrivilege.class) //
				.withElement(newClassPrivilege() //
						.withId("baz") //
						.withName("Baz") //
						.withDescription("This is baz") //
						.withMode("another mode") //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		when(service.read(anyString(), anyString())) //
				.thenReturn(expectedResponse);

		// when
		final HttpGet get = new HttpGet(server.resource("roles/foo/classes_privileges/bar/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("foo"), eq("bar"));
	}

}
