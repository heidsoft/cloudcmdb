package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.cxf.util.Json.safeJsonArray;
import static org.cmdbuild.service.rest.v1.cxf.util.Json.safeJsonObject;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessInstanceAdvance;
import static org.cmdbuild.workflow.service.WSProcessInstanceState.OPEN;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.data.store.lookup.LookupImpl;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v1.cxf.CxfProcessInstances;
import org.cmdbuild.service.rest.v1.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v1.model.ProcessInstance;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.LookupHelper;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.base.Optional;

public class CxfProcessInstancesTest {

	private static final Map<String, Object> NO_WIDGETS = Collections.emptyMap();

	private static final Map<String, Object> VALUES = ChainablePutMap.of(new HashMap<String, Object>()) //
			.chainablePut("foo", asList("oof")) //
			.chainablePut("bar", asList("rab")) //
			.chainablePut("baz", asList("zab"));

	private ErrorHandler errorHandler;
	private WorkflowLogic workflowLogic;
	private LookupHelper lookupHelper;

	private CxfProcessInstances cxfProcessInstances;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		workflowLogic = mock(WorkflowLogic.class);
		lookupHelper = mock(LookupHelper.class);
		cxfProcessInstances = new CxfProcessInstances(errorHandler, workflowLogic, lookupHelper);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenCreatingInstanceButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstances.create("123", newProcessInstanceAdvance() //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenCreatingInstanceButBusinessLogicThrowsException() throws Exception {
		// given
		final UserProcessClass userProcessClass = mockProcessClass(null, null);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final CMWorkflowException workflowException = new CMWorkflowException("error");
		doThrow(workflowException) //
				.when(workflowLogic).startProcess(anyString(), anyMapOf(String.class, String.class),
						anyMapOf(String.class, Object.class), anyBoolean());
		doThrow(new WebApplicationException(workflowException)) //
				.when(errorHandler).propagate(workflowException);

		// when
		cxfProcessInstances.create("123", newProcessInstanceAdvance() //
				.withValues(VALUES) //
				.withAdvance(true) //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq(123L));
		inOrder.verify(workflowLogic).startProcess(eq(123L), eq(VALUES), eq(NO_WIDGETS), eq(true));
		inOrder.verify(errorHandler).propagate(workflowException);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void businessLogicCalledSuccessfullyWhenCreatingInstance() throws Exception {
		// given
		final UserProcessClass userProcessClass = mockProcessClass(null, null);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserProcessInstance userProcessInstance = mock(UserProcessInstance.class);
		doReturn(123L) //
				.when(userProcessInstance).getId();
		doReturn(userProcessInstance) //
				.when(workflowLogic).startProcess(anyString(), anyMapOf(String.class, String.class),
						anyMapOf(String.class, Object.class), anyBoolean());

		// when
		final ResponseSingle<Long> response = cxfProcessInstances.create("123", newProcessInstanceAdvance() //
				.withValues(VALUES) //
				.withAdvance(true) //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).startProcess(eq("123"), eq(VALUES), eq(NO_WIDGETS), eq(true));
		inOrder.verifyNoMoreInteractions();
		assertThat(response.getElement(), equalTo(123L));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingAllInstancesButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstances.read("123", null, null, null, null, null);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void allInstancesReturned() throws Exception {
		// given
		final CMAttribute attribute = mock(CMAttribute.class);
		doReturn(new StringAttributeType()) //
				.when(attribute).getType();
		final UserProcessClass userProcessClass = mockProcessClass(null, "foo");
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(asList(attribute)) //
				.when(userProcessClass).getActiveAttributes();
		doReturn(attribute) //
				.when(userProcessClass).getAttribute(anyString());
		final UserProcessInstance foo = mockProcessInstance(123L, userProcessClass);
		doReturn("foo") //
				.when(foo).getProcessInstanceId();
		doReturn(OPEN) //
				.when(foo).getState();
		doReturn(VALUES.entrySet()) //
				.when(foo).getAllValues();
		final UserProcessInstance bar = mockProcessInstance(456L, userProcessClass);
		doReturn("bar") //
				.when(bar).getProcessInstanceId();
		doReturn(OPEN) //
				.when(bar).getState();
		doReturn(VALUES.entrySet()) //
				.when(bar).getAllValues();
		final PagedElements<UserProcessInstance> pagedElements = new PagedElements<UserProcessInstance>(
				asList(foo, bar), 4);
		doReturn(pagedElements) //
				.when(workflowLogic).query(anyString(), any(QueryOptions.class));
		doReturn(Optional.of(LookupImpl.newInstance().build())) //
				.when(lookupHelper).lookupForState(any(WSProcessInstanceState.class));

		// when
		final ResponseMultiple<ProcessInstance> response = cxfProcessInstances.read("123", "{\"the\": \"filter\"}",
				"[\"foo\", \"bar\", \"baz\"]", null, null, null);

		// then
		final ArgumentCaptor<QueryOptions> queryOptionsCaptor = ArgumentCaptor.forClass(QueryOptions.class);
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).query(eq("foo"), queryOptionsCaptor.capture());
		inOrder.verify(lookupHelper, times(2)).lookupForState(eq(OPEN));
		inOrder.verifyNoMoreInteractions();
		final QueryOptions captured = queryOptionsCaptor.getValue();
		assertThat(captured.getFilter().toString(), equalTo(safeJsonObject("{\"the\": \"filter\"}").toString()));
		assertThat(captured.getSorters().toString(), equalTo(safeJsonArray("[\"foo\", \"bar\", \"baz\"]").toString()));
		assertThat(captured.getLimit(), equalTo(Integer.MAX_VALUE));
		assertThat(captured.getOffset(), equalTo(0));
		assertThat(response.getMetadata().getTotal(), equalTo(4L));
		final Iterable<ProcessInstance> elements = response.getElements();
		assertThat(size(elements), equalTo(2));
		assertThat(get(elements, 0).getName(), equalTo("foo"));
		assertThat(get(elements, 1).getName(), equalTo("bar"));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingInstanceButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(eq("123"));

		// when
		cxfProcessInstances.read("123", 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingInstanceButInstanceNotFound() throws Exception {
		// given
		final UserProcessClass userProcessClass = mockProcessClass(null, null);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final PagedElements<UserProcessInstance> noElements = new PagedElements<UserProcessInstance>(null, 0);
		doReturn(noElements) //
				.when(workflowLogic).query(anyString(), any(QueryOptions.class));
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processInstanceNotFound(anyLong());

		// when
		cxfProcessInstances.read("123", 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq(123L));
		inOrder.verify(workflowLogic).query(eq("foo"), any(QueryOptions.class));
		inOrder.verify(errorHandler).processInstanceNotFound(456L);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void singleInstanceReturned() throws Exception {
		// given
		final CMAttribute attribute = mock(CMAttribute.class);
		doReturn(new StringAttributeType()) //
				.when(attribute).getType();
		final UserProcessClass userProcessClass = mockProcessClass(789L, "foo");
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(attribute) //
				.when(userProcessClass).getAttribute(anyString());
		final UserProcessInstance instance = mock(UserProcessInstance.class);
		doReturn(userProcessClass) //
				.when(instance).getType();
		doReturn(123L) //
				.when(instance).getId();
		doReturn("foo") //
				.when(instance).getProcessInstanceId();
		doReturn(OPEN) //
				.when(instance).getState();
		doReturn(VALUES.entrySet()) //
				.when(instance).getAllValues();
		final PagedElements<UserProcessInstance> pagedElements = new PagedElements<UserProcessInstance>(
				asList(instance), 1);
		doReturn(pagedElements) //
				.when(workflowLogic).query(anyString(), any(QueryOptions.class));
		doReturn(Optional.of(LookupImpl.newInstance().build())) //
				.when(lookupHelper).lookupForState(any(WSProcessInstanceState.class));

		// when
		final ResponseSingle<ProcessInstance> response = cxfProcessInstances.read("123", 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).query(eq("foo"), any(QueryOptions.class));
		inOrder.verify(lookupHelper).lookupForState(eq(OPEN));
		inOrder.verifyNoMoreInteractions();
		final ProcessInstance element = response.getElement();
		assertThat(element.getType(), equalTo("foo"));
		assertThat(element.getId(), equalTo(123L));
		assertThat(element.getName(), equalTo("foo"));
		assertThat(element.getValues(), equalTo(VALUES));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenUpdatingInstanceButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstances.update("123", 456L, newProcessInstanceAdvance().build());

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenUpdatingInstanceButBusinessLogicThrowsException() throws Exception {
		// given
		final UserProcessClass userProcessClass = mockProcessClass(null, null);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserActivityInstance userActivityInstance = mock(UserActivityInstance.class);
		doReturn("activity") //
				.when(userActivityInstance).getId();
		final UserProcessInstance userProcessInstance = mock(UserProcessInstance.class);
		doReturn(userActivityInstance) //
				.when(userProcessInstance).getActivityInstance(anyString());
		doReturn(userProcessInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		final CMWorkflowException workflowException = new CMWorkflowException("error");
		doThrow(workflowException) //
				.when(workflowLogic).updateProcess(anyString(), anyLong(), anyString(),
						anyMapOf(String.class, String.class), anyMapOf(String.class, Object.class), anyBoolean());
		doThrow(new WebApplicationException(workflowException)) //
				.when(errorHandler).propagate(workflowException);

		// when
		cxfProcessInstances.update("123", 456L, newProcessInstanceAdvance() //
				.withValues(VALUES) //
				.withActivity("activity") //
				.withAdvance(true) //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("123"), eq(456L));
		inOrder.verify(workflowLogic).updateProcess(eq("123"), eq(456L), eq("activity"), eq(VALUES), eq(NO_WIDGETS),
				eq(true));
		inOrder.verify(errorHandler).propagate(workflowException);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void businessLogicCalledSuccessfullyWhenUpdatingInstance() throws Exception {
		// given
		final String activityId = "activity";
		final UserProcessClass userProcessClass = mockProcessClass(null, null);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserActivityInstance userActivityInstance = mock(UserActivityInstance.class);
		doReturn("activity") //
				.when(userActivityInstance).getId();
		final UserProcessInstance userProcessInstance = mock(UserProcessInstance.class);
		doReturn(123L) //
				.when(userProcessInstance).getId();
		doReturn(userActivityInstance) //
				.when(userProcessInstance).getActivityInstance(anyString());
		doReturn(userProcessInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doReturn(userProcessInstance) //
				.when(workflowLogic).updateProcess(anyString(), anyLong(), anyString(),
						anyMapOf(String.class, String.class), anyMapOf(String.class, Object.class), anyBoolean());

		// when
		cxfProcessInstances.update("123", 456L, newProcessInstanceAdvance() //
				.withValues(VALUES) //
				.withActivity(activityId) //
				.withAdvance(true) //
				.build());

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("123"), eq(456L));
		inOrder.verify(workflowLogic).updateProcess(eq("123"), eq(456L), eq(activityId), eq(VALUES), eq(NO_WIDGETS),
				eq(true));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenDeletingInstanceButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstances.delete("123", 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("456"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenDeletingInstanceButBusinessLogicThrowsException() throws Exception {
		// given
		final UserProcessClass userProcessClass = mockProcessClass(null, null);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final CMWorkflowException workflowException = new CMWorkflowException("error");
		doThrow(workflowException) //
				.when(workflowLogic).abortProcess(anyString(), anyLong());
		doThrow(new WebApplicationException(workflowException)) //
				.when(errorHandler).propagate(workflowException);

		// when
		cxfProcessInstances.delete("123", 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(123L);
		inOrder.verify(workflowLogic).abortProcess(123L, 456L);
		inOrder.verify(errorHandler).propagate(workflowException);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void businessLogicCalledSuccessfullyWhenDeletingInstance() throws Exception {
		// given
		final UserProcessClass userProcessClass = mockProcessClass(null, null);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());

		// when
		cxfProcessInstances.delete("123", 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, lookupHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).abortProcess(eq("123"), eq(456L));
		inOrder.verifyNoMoreInteractions();
	}

	private UserProcessClass mockProcessClass(final Long id, final String name) {
		final UserProcessClass mock = mock(UserProcessClass.class);
		doReturn(id) //
				.when(mock).getId();
		doReturn(name) //
				.when(mock).getName();
		return mock;
	}

	private UserProcessInstance mockProcessInstance(final Long id, final UserProcessClass type) {
		final UserProcessInstance mock = mock(UserProcessInstance.class);
		doReturn(id) //
				.when(mock).getId();
		doReturn(type) //
				.when(mock).getType();
		return mock;
	}

}
