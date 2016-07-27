package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newAttribute;
import static org.cmdbuild.service.rest.v2.model.Models.newLongIdAndDescription;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newReport;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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
import org.cmdbuild.service.rest.v2.Reports;
import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.LongIdAndDescription;
import org.cmdbuild.service.rest.v2.model.Report;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ReportsTest {

	@ClassRule
	public static ServerResource<Reports> server = ServerResource.newInstance(Reports.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Reports service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Reports.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void readAll() throws Exception {
		// given
		final ResponseMultiple<LongIdAndDescription> sentResponse = newResponseMultiple(LongIdAndDescription.class) //
				.withElements(asList( //
						newLongIdAndDescription() //
								.withId(1L) //
								.withDescription("foo") //
								.build(), //
						newLongIdAndDescription() //
								.withId(2L) //
								.withDescription("bar") //
								.build() //
		)) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build())
				.build();
		doReturn(sentResponse) //
				.when(service).readAll(anyString(), anyInt(), anyInt());

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("reports/")) //
				.setParameter(FILTER, "filter") //
				.setParameter(LIMIT, "12") //
				.setParameter(START, "34") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(sentResponse)));

		verify(service).readAll(eq("filter"), eq(12), eq(34));
	}

	@Test
	public void read() throws Exception {
		// given
		final ResponseSingle<Report> sentResponse = newResponseSingle(Report.class) //
				.withElement(newReport() //
						.withId(12L) //
						.withTitle("this is the title") //
						.withDescription("this is the description") //
						.build()) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyLong());

		// when
		final HttpGet get = new HttpGet(server.resource("reports/34/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(sentResponse)));

		verify(service).read(eq(34L));
	}

	@Test
	public void readAllAttributes() throws Exception {
		// given
		final ResponseMultiple<Attribute> sentResponse = newResponseMultiple(Attribute.class) //
				.withElements(asList( //
						newAttribute() //
								.withName("bar") //
								.build(), //
						newAttribute() //
								.withName("baz") //
								.build())) //
				.withMetadata(newMetadata() //
						.withTotal(42L) //
						.build()) //
				.build();
		when(service.readAllAttributes(anyLong(), anyInt(), anyInt())) //
				.thenReturn(sentResponse);

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("reports/123/attributes/")) //
				.setParameter(LIMIT, "456") //
				.setParameter(START, "789") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(sentResponse)));

		verify(service).readAllAttributes(eq(123L), eq(456), eq(789));
	}

}
