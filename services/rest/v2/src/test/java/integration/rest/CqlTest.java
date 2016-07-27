package integration.rest;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.SORT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.v2.model.Models.newCard;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.Cql;
import org.cmdbuild.service.rest.v2.model.Card;
import org.cmdbuild.service.rest.v2.model.Models;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class CqlTest {

	@ClassRule
	public static ServerResource<Cql> server = ServerResource.newInstance(Cql.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Cql service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Cql.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void query() throws Exception {
		// given
		final String type = "some type";
		final Long firstId = 12L;
		final Long secondId = 34L;
		final Map<String, String> firstValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz");
		final Map<String, String> secondValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("bar", "baz");
		final ResponseMultiple<Card> sentResponse = newResponseMultiple(Card.class) //
				.withElements(asList( //
						newCard() //
								.withType(type) //
								.withId(firstId) //
								.withValues(firstValues) //
								.build(), //
						newCard() //
								.withType(type) //
								.withId(secondId) //
								.withValues(secondValues) //
								.build() //
		)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		@SuppressWarnings("unchecked")
		final ResponseMultiple<Map<String, Object>> expectedResponse = Models
				.<Map<String, Object>> newResponseMultiple() //
				.withElements(Arrays.<Map<String, Object>> asList( //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_TYPE, type) //
								.chainablePut(UNDERSCORED_ID, firstId) //
								.chainablePutAll(firstValues), //
						ChainablePutMap.of(new HashMap<String, Object>()) //
								.chainablePut(UNDERSCORED_TYPE, type) //
								.chainablePut(UNDERSCORED_ID, secondId) //
								.chainablePutAll(secondValues) //
		)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyString(), anyInt(), anyInt());

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("cql/")) //
				.setParameter(FILTER, "this is the filter") //
				.setParameter(SORT, "sorting options") //
				.setParameter(LIMIT, "56") //
				.setParameter(START, "78") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("this is the filter"), eq("sorting options"), eq(56), eq(78));
	}

}
