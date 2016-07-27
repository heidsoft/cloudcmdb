package integration.rest;

import static com.google.common.collect.Iterables.get;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.test.HttpClientUtils.contentOf;
import static org.cmdbuild.service.rest.test.HttpClientUtils.statusCodeOf;
import static org.cmdbuild.service.rest.test.ServerResource.randomPort;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.SORT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newProcessInstance;
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

import java.util.Collection;
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
import org.cmdbuild.service.rest.v2.ProcessInstances;
import org.cmdbuild.service.rest.v2.model.Models;
import org.cmdbuild.service.rest.v2.model.ProcessInstance;
import org.cmdbuild.service.rest.v2.model.ProcessInstanceAdvanceable;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Widget;
import org.cmdbuild.service.rest.v2.model.adapter.ProcessInstanceAdapter;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ProcessInstancesTest {

	@ClassRule
	public static ServerResource<ProcessInstances> server = ServerResource.newInstance(ProcessInstances.class) //
			.withPortRange(randomPort()) //
			.build();

	private static JsonSupport json = new JsonSupport();

	private ProcessInstances service;
	private HttpClient httpclient;
	private ProcessInstanceAdapter adapter;

	@Before
	public void createHttpClient() throws Exception {
		server.service(service = mock(ProcessInstances.class));
		httpclient = HttpClientBuilder.create().build();
		adapter = new ProcessInstanceAdapter();
	}

	@Test
	public void instanceCreatedWithMissingWidgets() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(123L) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), any(ProcessInstanceAdvanceable.class));

		// when
		final HttpPost post = new HttpPost(server.resource("processes/12/instances/"));
		post.setEntity(new StringEntity(
				EMPTY //
						+ "{"//
						+ "    \"_id\" : 34," //
						+ "    \"_type\" : \"56\"," //
						+ "    \"_advance\" : true," //
						+ "    \"bar\" : \"BAR\"," //
						+ "    \"baz\" : \"BAZ\"" //
						+ "}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<ProcessInstanceAdvanceable> captor = ArgumentCaptor
				.forClass(ProcessInstanceAdvanceable.class);
		verify(service).create(eq("12"), captor.capture());
		final ProcessInstanceAdvanceable captured = captor.getValue();
		assertThat(captured.getId(), equalTo(34L));
		assertThat(captured.getType(), equalTo("56"));
		assertThat(captured.isAdvance(), equalTo(true));
		assertThat(captured.getValues(), hasEntry("bar", (Object) "BAR"));
		assertThat(captured.getValues(), hasEntry("baz", (Object) "BAZ"));
		final Collection<Widget> widgets = captured.getWidgets();
		assertThat(widgets.size(), equalTo(0));
	}

	@Test
	public void instanceCreatedWithNoWidgets() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(123L) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), any(ProcessInstanceAdvanceable.class));

		// when
		final HttpPost post = new HttpPost(server.resource("processes/12/instances/"));
		post.setEntity(new StringEntity(
				EMPTY //
						+ "{"//
						+ "    \"_id\" : 34," //
						+ "    \"_type\" : \"56\"," //
						+ "    \"_advance\" : true," //
						+ "    \"bar\" : \"BAR\"," //
						+ "    \"baz\" : \"BAZ\"," //
						+ "    \"_widgets\" : [" //
						+ "    ]" //
						+ "}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<ProcessInstanceAdvanceable> captor = ArgumentCaptor
				.forClass(ProcessInstanceAdvanceable.class);
		verify(service).create(eq("12"), captor.capture());
		final ProcessInstanceAdvanceable captured = captor.getValue();
		assertThat(captured.getId(), equalTo(34L));
		assertThat(captured.getType(), equalTo("56"));
		assertThat(captured.isAdvance(), equalTo(true));
		assertThat(captured.getValues(), hasEntry("bar", (Object) "BAR"));
		assertThat(captured.getValues(), hasEntry("baz", (Object) "BAZ"));
		final Collection<Widget> widgets = captured.getWidgets();
		assertThat(widgets.size(), equalTo(0));
	}

	@Test
	public void instanceCreatedWithWidgets() throws Exception {
		// given
		final ResponseSingle<Long> expectedResponse = newResponseSingle(Long.class) //
				.withElement(123L) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(expectedResponse) //
				.when(service).create(anyString(), any(ProcessInstanceAdvanceable.class));

		// when
		final HttpPost post = new HttpPost(server.resource("processes/12/instances/"));
		post.setEntity(new StringEntity(
				EMPTY //
						+ "{"//
						+ "    \"_id\" : 34," //
						+ "    \"_type\" : \"56\"," //
						+ "    \"_advance\" : true," //
						+ "    \"bar\" : \"BAR\"," //
						+ "    \"baz\" : \"BAZ\"," //
						+ "    \"_widgets\" : [" //
						+ "        {" //
						+ "            \"_id\" : \"widget 1\"," //
						+ "            \"output\" : \"output 1\"" //
						+ "        }," //
						+ "        {" //
						+ "            \"_id\" : \"widget 2\"," //
						+ "            \"output\" : \"output 2\"" //
						+ "        }" //
						+ "    ]" //
						+ "}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(post);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		final ArgumentCaptor<ProcessInstanceAdvanceable> captor = ArgumentCaptor
				.forClass(ProcessInstanceAdvanceable.class);
		verify(service).create(eq("12"), captor.capture());
		final ProcessInstanceAdvanceable captured = captor.getValue();
		assertThat(captured.getId(), equalTo(34L));
		assertThat(captured.getType(), equalTo("56"));
		assertThat(captured.isAdvance(), equalTo(true));
		assertThat(captured.getValues(), hasEntry("bar", (Object) "BAR"));
		assertThat(captured.getValues(), hasEntry("baz", (Object) "BAZ"));
		final Collection<Widget> widgets = captured.getWidgets();
		assertThat(widgets.size(), equalTo(2));
		final Widget widget1 = get(widgets, 0);
		assertThat(widget1.getId(), equalTo("widget 1"));
		assertThat(widget1.getOutput(), equalTo((Object) "output 1"));
		final Widget widget2 = get(widgets, 1);
		assertThat(widget2.getId(), equalTo("widget 2"));
		assertThat(widget2.getOutput(), equalTo((Object) "output 2"));
	}

	@Test
	public void instanceRead() throws Exception {
		// given
		final ProcessInstance processInstance = newProcessInstance() //
				.withType("123") //
				.withId(456L) //
				.withName("foo") //
				.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz")) //
				.build();
		final ResponseSingle<ProcessInstance> sentResponse = newResponseSingle(ProcessInstance.class) //
				.withElement(processInstance) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		final ResponseSingle<Map<String, Object>> expectedResponse = Models.<Map<String, Object>> newResponseSingle() //
				.withElement(adapter.marshal(processInstance)) //
				.withMetadata(newMetadata() //
						// nothing to add, just needed for simplify assertions
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyLong());

		// when
		final HttpGet get = new HttpGet(server.resource("processes/123/instances/456/"));
		final HttpResponse response = httpclient.execute(get);

		// then
		assertThat(statusCodeOf(response), equalTo(200));
		assertThat(json.from(contentOf(response)), equalTo(json.from(expectedResponse)));

		verify(service).read(eq("123"), eq(456L));
	}

	@Test
	public void instancesRead() throws Exception {
		// given
		final ProcessInstance first = newProcessInstance() //
				.withType("12") //
				.withId(34L) //
				.withName("foo") //
				.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("foo", "bar") //
						.chainablePut("bar", "baz")) //
				.build();
		final ProcessInstance second = newProcessInstance() //
				.withType("12") //
				.withId(56L) //
				.withName("bar") //
				.withValues(ChainablePutMap.of(new HashMap<String, Object>()) //
						.chainablePut("bar", "baz")) //
				.build();
		final ResponseMultiple<ProcessInstance> sentResponse = newResponseMultiple(ProcessInstance.class) //
				.withElements(asList(first, second)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		final ResponseMultiple<Map<String, Object>> expectedResponse = Models
				.<Map<String, Object>> newResponseMultiple() //
				.withElement(adapter.marshal(first)) //
				.withElement(adapter.marshal(second)) //
				.withMetadata(newMetadata() //
						.withTotal(2L) //
						.build()) //
				.build();
		doReturn(sentResponse) //
				.when(service).read(anyString(), anyString(), anyString(), anyInt(), anyInt(), any(Set.class));

		// when
		final HttpGet get = new HttpGet(new URIBuilder(server.resource("processes/12/instances")) //
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
		verify(service).read(eq("12"), eq("filter"), eq("sort"), eq(456), eq(789), captor.capture());
		assertThat(captor.getValue().isEmpty(), equalTo(true));
	}

	@Test
	public void instanceUpdatedWithMissingWidgets() throws Exception {
		// when
		final HttpPut put = new HttpPut(server.resource("processes/12/instances/34/"));
		put.setEntity(new StringEntity(
				EMPTY //
						+ "{" //
						+ "    \"_id\" : 56," //
						+ "    \"_type\" : \"78\"," //
						+ "    \"_activity\" : \"90\"," //
						+ "    \"_advance\" : true," //
						+ "    \"bar\" : \"BAR\"," //
						+ "    \"baz\" : \"BAZ\"" //
						+ "}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<ProcessInstanceAdvanceable> captor = ArgumentCaptor
				.forClass(ProcessInstanceAdvanceable.class);
		verify(service).update(eq("12"), eq(34L), captor.capture());
		final ProcessInstanceAdvanceable captured = captor.getValue();
		assertThat(captured.getActivity(), equalTo("90"));
		assertThat(captured.isAdvance(), equalTo(true));
		assertThat(captured.getValues(), hasEntry("bar", (Object) "BAR"));
		assertThat(captured.getValues(), hasEntry("baz", (Object) "BAZ"));
		final Collection<Widget> widgets = captured.getWidgets();
		assertThat(widgets.size(), equalTo(0));
	}

	@Test
	public void instanceUpdatedWithNoWidgets() throws Exception {
		// when
		final HttpPut put = new HttpPut(server.resource("processes/12/instances/34/"));
		put.setEntity(new StringEntity(
				EMPTY //
						+ "{" //
						+ "    \"_id\" : 56," //
						+ "    \"_type\" : \"78\"," //
						+ "    \"_activity\" : \"90\"," //
						+ "    \"_advance\" : true," //
						+ "    \"bar\" : \"BAR\"," //
						+ "    \"baz\" : \"BAZ\"," //
						+ "    \"_widgets\" : [" //
						+ "    ]" //
						+ "}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<ProcessInstanceAdvanceable> captor = ArgumentCaptor
				.forClass(ProcessInstanceAdvanceable.class);
		verify(service).update(eq("12"), eq(34L), captor.capture());
		final ProcessInstanceAdvanceable captured = captor.getValue();
		assertThat(captured.getActivity(), equalTo("90"));
		assertThat(captured.isAdvance(), equalTo(true));
		assertThat(captured.getValues(), hasEntry("bar", (Object) "BAR"));
		assertThat(captured.getValues(), hasEntry("baz", (Object) "BAZ"));
		final Collection<Widget> widgets = captured.getWidgets();
		assertThat(widgets.size(), equalTo(0));
	}

	@Test
	public void instanceUpdatedWithWidgets() throws Exception {
		// when
		final HttpPut put = new HttpPut(server.resource("processes/12/instances/34/"));
		put.setEntity(new StringEntity(
				EMPTY //
						+ "{" //
						+ "    \"_id\" : 56," //
						+ "    \"_type\" : \"78\"," //
						+ "    \"_activity\" : \"90\"," //
						+ "    \"_advance\" : true," //
						+ "    \"bar\" : \"BAR\"," //
						+ "    \"baz\" : \"BAZ\"," //
						+ "    \"_widgets\" : [" //
						+ "        {" //
						+ "            \"_id\" : \"widget 1\"," //
						+ "            \"output\" : \"output 1\"" //
						+ "        }," //
						+ "        {" //
						+ "            \"_id\" : \"widget 2\"," //
						+ "            \"output\" : \"output 2\"" //
						+ "        }" //
						+ "    ]" //
						+ "}", //
				APPLICATION_JSON) //
		);
		final HttpResponse response = httpclient.execute(put);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		final ArgumentCaptor<ProcessInstanceAdvanceable> captor = ArgumentCaptor
				.forClass(ProcessInstanceAdvanceable.class);
		verify(service).update(eq("12"), eq(34L), captor.capture());
		final ProcessInstanceAdvanceable captured = captor.getValue();
		assertThat(captured.getActivity(), equalTo("90"));
		assertThat(captured.isAdvance(), equalTo(true));
		assertThat(captured.getValues(), hasEntry("bar", (Object) "BAR"));
		assertThat(captured.getValues(), hasEntry("baz", (Object) "BAZ"));
		final Collection<Widget> widgets = captured.getWidgets();
		assertThat(widgets.size(), equalTo(2));
		final Widget widget1 = get(widgets, 0);
		assertThat(widget1.getId(), equalTo("widget 1"));
		assertThat(widget1.getOutput(), equalTo((Object) "output 1"));
		final Widget widget2 = get(widgets, 1);
		assertThat(widget2.getId(), equalTo("widget 2"));
		assertThat(widget2.getOutput(), equalTo((Object) "output 2"));
	}

	@Test
	public void instanceDeleted() throws Exception {
		// when
		final HttpDelete delete = new HttpDelete(server.resource("processes/123/instances/456/"));
		final HttpResponse response = httpclient.execute(delete);

		// then
		assertThat(statusCodeOf(response), equalTo(204));

		verify(service).delete(eq("123"), eq(456L));
	}

}
