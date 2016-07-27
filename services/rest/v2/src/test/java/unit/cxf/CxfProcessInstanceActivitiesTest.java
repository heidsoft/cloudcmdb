package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstanceActivities;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ProcessActivityWithFullDetails.AttributeStatus;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class CxfProcessInstanceActivitiesTest {

	private ErrorHandler errorHandler;
	private WorkflowLogic workflowLogic;

	private CxfProcessInstanceActivities cxfProcessInstanceActivities;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		workflowLogic = mock(WorkflowLogic.class);
		cxfProcessInstanceActivities = new CxfProcessInstanceActivities(errorHandler, workflowLogic);
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingAllActivitiesButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstanceActivities.read("123", 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingAllActivitiesButInstanceNotFound() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final PagedElements<UserProcessInstance> noElements = new PagedElements<UserProcessInstance>(null, 0);
		doReturn(noElements) //
				.when(workflowLogic).query(any(CMClass.class), any(QueryOptions.class));
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processInstanceNotFound(anyLong());

		// when
		cxfProcessInstanceActivities.read("123", 456L);

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).query(userProcessClass, any(QueryOptions.class));
		inOrder.verify(errorHandler).processInstanceNotFound(eq(456L));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void allActivitiesReturned() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserActivityInstance onlyActivity = mock(UserActivityInstance.class);
		doReturn("foo") //
				.when(onlyActivity).getId();
		doReturn(true) //
				.when(onlyActivity).isWritable();
		final UserProcessInstance onlyInstance = mock(UserProcessInstance.class);
		doReturn(asList(onlyActivity)) //
				.when(onlyInstance).getActivities();
		final PagedElements<UserProcessInstance> pagedElements = new PagedElements<UserProcessInstance>(
				asList(onlyInstance), 1);
		doReturn(pagedElements) //
				.when(workflowLogic).query(any(CMClass.class), any(QueryOptions.class));

		// when
		final ResponseMultiple<ProcessActivityWithBasicDetails> response = cxfProcessInstanceActivities.read("123",
				456L);

		// then
		final ArgumentCaptor<QueryOptions> queryOptionsCaptor = ArgumentCaptor.forClass(QueryOptions.class);
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).query(eq(userProcessClass), queryOptionsCaptor.capture());
		inOrder.verifyNoMoreInteractions();
		final QueryOptions captured = queryOptionsCaptor.getValue();
		assertThat(captured.getLimit(), equalTo(1));
		assertThat(captured.getOffset(), equalTo(0));
		assertThat(response.getMetadata().getTotal(), equalTo(1L));
		final Iterable<ProcessActivityWithBasicDetails> elements = response.getElements();
		assertThat(size(elements), equalTo(1));
		final ProcessActivityWithBasicDetails firstElement = get(elements, 0);
		assertThat(firstElement.getId(), equalTo("foo"));
		assertThat(firstElement.isWritable(), equalTo(true));
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingActivityButProcessNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcessInstanceActivities.read("123", 456L, "789");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingActivityButInstanceNotFound() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(null) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processInstanceNotFound(anyLong());

		// when
		cxfProcessInstanceActivities.read("123", 456L, "789");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("123"), eq(456L));
		inOrder.verify(errorHandler).processInstanceNotFound(eq(456L));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingActivityButActivityNotFound() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(null) //
				.when(workflowLogic).getActivityInstance(eq("123"), eq(456L), eq("bar"));
		doThrow(WebApplicationException.class) //
				.when(errorHandler).processActivityNotFound(anyString());

		// when
		cxfProcessInstanceActivities.read("123", 456L, "789");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("123"), eq(456L));
		inOrder.verify(errorHandler).processActivityNotFound(eq("789"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = WebApplicationException.class)
	public void exceptionWhenReadingActivityButGetDefinitionThrowsException() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final UserActivityInstance userActivityInstance = mock(UserActivityInstance.class);
		doReturn("bar") //
				.when(userActivityInstance).getId();
		final CMWorkflowException exception = new CMWorkflowException("dummy");
		doThrow(exception) //
				.when(userActivityInstance).getDefinition();
		final UserProcessInstance userProcessInstance = mock(UserProcessInstance.class);
		doReturn(userActivityInstance) //
				.when(workflowLogic).getActivityInstance(eq("123"), eq(456L), eq("bar"));
		doReturn(userProcessInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).propagate(exception);

		// when
		cxfProcessInstanceActivities.read("123", 456L, "bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("123"), eq(456L));
		inOrder.verify(errorHandler).propagate(eq(exception));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void activityReturned() throws Exception {
		// given
		final UserProcessClass userProcessClass = mock(UserProcessClass.class);
		doReturn(userProcessClass) //
				.when(workflowLogic).findProcessClass(anyString());
		final CMActivity activity = mock(CMActivity.class);
		doReturn("bar") //
				.when(activity).getId();
		doReturn("description") //
				.when(activity).getDescription();
		doReturn("instructions") //
				.when(activity).getInstructions();
		doReturn(asList( //
				new CMActivityVariableToProcess("foo", false, false), //
				new CMActivityVariableToProcess("bar", true, false), //
				new CMActivityVariableToProcess("baz", true, true) //
				)) //
				.when(activity).getVariables();
		doReturn(activity) //
				.when(workflowLogic).getStartActivity(anyString());
		final UserActivityInstance userActivityInstance = mock(UserActivityInstance.class);
		doReturn("bar") //
				.when(userActivityInstance).getId();
		final CMWorkflowException exception = new CMWorkflowException("dummy");
		doReturn(activity) //
				.when(userActivityInstance).getDefinition();
		final UserProcessInstance userProcessInstance = mock(UserProcessInstance.class);
		doReturn(userActivityInstance) //
				.when(workflowLogic).getActivityInstance(anyString(), anyLong(), anyString());
		doReturn(userProcessInstance) //
				.when(workflowLogic).getProcessInstance(anyString(), anyLong());
		doThrow(WebApplicationException.class) //
				.when(errorHandler).propagate(exception);

		// when
		final ResponseSingle<ProcessActivityWithFullDetails> response = cxfProcessInstanceActivities.read("123", 456L,
				"bar");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(workflowLogic).getProcessInstance(eq("123"), eq(456L));
		inOrder.verify(workflowLogic).getActivityInstance(eq("123"), eq(456L), eq("bar"));
		inOrder.verifyNoMoreInteractions();
		final ProcessActivityWithFullDetails element = response.getElement();
		assertThat(element.getId(), equalTo(activity.getId()));
		assertThat(element.getDescription(), equalTo(activity.getDescription()));
		assertThat(element.getInstructions(), equalTo(activity.getInstructions()));
		final Iterable<AttributeStatus> attributes = element.getAttributes();
		assertThat(size(attributes), equalTo(3));
		final AttributeStatus fooReadOnly = get(attributes, 0);
		assertThat(fooReadOnly.getId(), equalTo("foo"));
		assertThat(fooReadOnly.isWritable(), equalTo(false));
		assertThat(fooReadOnly.isMandatory(), equalTo(false));
		final AttributeStatus barWriteableAndNotMandatory = get(attributes, 1);
		assertThat(barWriteableAndNotMandatory.getId(), equalTo("bar"));
		assertThat(barWriteableAndNotMandatory.isWritable(), equalTo(true));
		assertThat(barWriteableAndNotMandatory.isMandatory(), equalTo(false));
		final AttributeStatus bazWriteableAndMandatory = get(attributes, 2);
		assertThat(bazWriteableAndMandatory.getId(), equalTo("baz"));
		assertThat(bazWriteableAndMandatory.isWritable(), equalTo(true));
		assertThat(bazWriteableAndMandatory.isMandatory(), equalTo(true));
	}

}
