package integration.rest;

import static java.util.Arrays.asList;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
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
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.test.JsonSupport;
import org.cmdbuild.service.rest.test.ServerResource;
import org.cmdbuild.service.rest.v2.Cards;
import org.cmdbuild.service.rest.v2.model.Card;
import org.cmdbuild.service.rest.v2.model.Models;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class CardsTest {

	@ClassRule
	public static ServerResource<Cards> server = ServerResource.newInstance(Cards.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private Cards service;
	private HttpClient httpclient;

	@Before
	public void setUp() throws Exception {
		server.service(service = mock(Cards.class));
		httpclient = HttpClientBuilder.create().build();
	}

	@Test
	public void cardsRead() throws Exception {
		// given
		final String type = "123L";
		final Long firstId = 456L;
		final Long secondId = 789L;
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
				.when(service).read(anyString(), anyString(), anyString(), anyInt(), anyInt(), any(Set.class));

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("classes/123/cards")) //
				.setParameter(FILTER, "filter") //
				.setParameter(SORT, "sort") //
				.setParameter(LIMIT, "456") //
				.setParameter(START, "789") //
				.build());
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);
		verify(service).read(eq("123"), eq("filter"), eq("sort"), eq(456), eq(789), captor.capture());
		assertThat(captor.getValue().isEmpty(), equalTo(true));
	}

	@Test
	public void cardCreated() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(123L) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), any(Card.class));

		// when
		final HttpPost post = new HttpPost(server.resource("classes/12/cards/"));
		post.setEntity(new StringEntity( //
				"{\"_id\" : 34, \"_type\" : \"56\", \"bar\" : \"BAR\", \"baz\" : \"BAZ\"}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(service).create(eq("12"), cardCaptor.capture());

		final Card captured = cardCaptor.getValue();
		assertThat(captured.getType(), equalTo("56"));
		assertThat(captured.getId(), equalTo(34L));

		final Map<String, Object> values = captured.getValues();
		assertThat(values, hasEntry("bar", (Object) "BAR"));
		assertThat(values, hasEntry("baz", (Object) "BAZ"));
	}

	@Test
	public void cardRead() throws Exception {
		// given
		final String type = "123L";
		final Long firstId = 456L;
		final Map<String, String> firstValues = ChainablePutMap.of(new HashMap<String, String>()) //
				.chainablePut("foo", "bar") //
				.chainablePut("bar", "baz") //
				.chainablePut("bar", "baz");
		final ResponseSingle<Card> sentResponse = newResponseSingle(Card.class) //
				.withElement(newCard() //
						.withType(type) //
						.withId(firstId) //
						.withValues(firstValues) //
						.build() //
		) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		final ResponseSingle<Map<String, Object>> expectedResponse = Models.<Map<String, Object>> newResponseSingle() //
				.withElement(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut(UNDERSCORED_TYPE, type) //
						.chainablePut(UNDERSCORED_ID, firstId) //
						.chainablePutAll(firstValues) //
		) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong());

		// when
		final HttpGet get = new HttpGet(server.resource("classes/123/cards/456/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("123"), eq(456L));
	}

	@Test
	public void cardUpdated() throws Exception {
		// when
		final HttpPut put = new HttpPut(server.resource("classes/12/cards/34/"));
		put.setEntity(new StringEntity( //
				"{\"_id\" : 56, \"_type\" : \"78\", \"bar\" : \"BAR\", \"baz\" : \"BAZ\"}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
		verify(service).update(eq("12"), eq(34L), cardCaptor.capture());

		final Card captured = cardCaptor.getValue();
		assertThat(captured.getType(), equalTo("78"));
		assertThat(captured.getId(), equalTo(56L));

		final Map<String, Object> values = captured.getValues();
		assertThat(values, hasEntry("bar", (Object) "BAR"));
		assertThat(values, hasEntry("baz", (Object) "BAZ"));
	}

	@Test
	public void cardDeleted() throws Exception {
		// when
		final HttpDelete delete = new HttpDelete(server.resource("classes/123/cards/456/"));
		final HttpResponse response = httpclient.execute(delete);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		verify(service).delete(eq("123"), eq(456L));
	}

}
