package unit.cxf;

import static com.google.common.collect.Iterables.get;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessStatus;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v1.cxf.CxfProcesses;
import org.cmdbuild.service.rest.v1.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v1.cxf.ProcessStatusHelper;
import org.cmdbuild.service.rest.v1.model.ProcessWithBasicDetails;
import org.cmdbuild.service.rest.v1.model.ProcessWithFullDetails;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.base.Optional;

public class CxfProcessesTest {

	private static final Iterable<UserProcessClass> NO_PROCESSES = Collections.emptyList();

	private ErrorHandler errorHandler;
	private WorkflowLogic workflowLogic;
	private ProcessStatusHelper processStatusHelper;

	private CxfProcesses cxfProcesses;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		workflowLogic = mock(WorkflowLogic.class);
		processStatusHelper = mock(ProcessStatusHelper.class);
		cxfProcesses = new CxfProcesses(errorHandler, workflowLogic, processStatusHelper);
	}

	@Test
	public void noProcessesWhenTheyAreMissingWithinLogic() throws Exception {
		// given
		when(workflowLogic.findProcessClasses(anyBoolean())) //
				.thenReturn(NO_PROCESSES);

		// when
		final ResponseMultiple<ProcessWithBasicDetails> response = cxfProcesses.readAll(true, null, null);

		// then
		assertThat(response.getElements(), hasSize(0));
		assertThat(response.getMetadata().getTotal(), equalTo(0L));
		verify(workflowLogic).findProcessClasses(true);
		verifyNoMoreInteractions(errorHandler, workflowLogic, processStatusHelper);
	}

	@Test
	public void processesReturnedSortedByName() throws Exception {
		// given
		final UserProcessClass foo = mock(UserProcessClass.class);
		{
			when(foo.getName()).thenReturn("foo");
			when(foo.getDescription()).thenReturn("Foo");
			when(foo.isSuperclass()).thenReturn(true);
			when(foo.getParent()).thenReturn(null);
		}
		final UserProcessClass bar = mock(UserProcessClass.class);
		{
			when(bar.getName()).thenReturn("bar");
			when(bar.getDescription()).thenReturn("Bar");
			when(bar.isSuperclass()).thenReturn(false);
			when(bar.getParent()).thenReturn(foo);
		}
		when(workflowLogic.findProcessClasses(anyBoolean())) //
				.thenReturn(asList(foo, bar));

		// when
		final ResponseMultiple<ProcessWithBasicDetails> response = cxfProcesses.readAll(true, null, null);

		// then
		assertThat(response.getElements(), hasSize(2));
		assertThat(response.getMetadata().getTotal(), equalTo(2L));
		final ProcessWithBasicDetails first = get(response.getElements(), 0);
		assertThat(first.getName(), equalTo("bar"));
		assertThat(first.getDescription(), equalTo("Bar"));
		assertThat(first.getParent(), equalTo("foo"));
		assertThat(first.isPrototype(), equalTo(false));
		final ProcessWithBasicDetails second = get(response.getElements(), 1);
		assertThat(second.getName(), equalTo("foo"));
		assertThat(second.getDescription(), equalTo("Foo"));
		assertThat(second.getParent(), equalTo(null));
		assertThat(second.isPrototype(), equalTo(true));
		verify(workflowLogic).findProcessClasses(true);
		verifyNoMoreInteractions(errorHandler, workflowLogic, processStatusHelper);
	}

	@Test(expected = WebApplicationException.class)
	public void processNotFound() throws Exception {
		// given
		doReturn(null) //
				.when(workflowLogic).findProcessClass(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).processNotFound(anyString());

		// when
		cxfProcesses.read("123");

		// then
		final InOrder inOrder = inOrder(errorHandler, workflowLogic, processStatusHelper);
		inOrder.verify(workflowLogic).findProcessClass(eq("123"));
		inOrder.verify(errorHandler).processNotFound(eq("123"));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void processFound() throws Exception {
		// given
		final UserProcessClass foo = mock(UserProcessClass.class);
		{
			when(foo.getName()).thenReturn("foo");
			when(foo.getDescription()).thenReturn("Foo");
			when(foo.isSuperclass()).thenReturn(true);
			when(foo.getParent()).thenReturn(null);
		}
		doReturn(foo) //
				.when(workflowLogic).findProcessClass(anyString());
		doReturn(asList( //
				newProcessStatus() //
						.withId(123L) //
						.build(), //
				newProcessStatus() //
						.withId(456L) //
						.build() //
				)) //
				.when(processStatusHelper).allValues();
		doReturn(Optional.of(newProcessStatus() //
				.withId(789L) //
				.build())) //
				.when(processStatusHelper).defaultValue();

		// when
		final ResponseSingle<ProcessWithFullDetails> response = cxfProcesses.read("123");

		// then
		final ProcessWithFullDetails element = response.getElement();
		assertThat(element.getName(), equalTo("foo"));
		assertThat(element.getDescription(), equalTo("Foo"));
		assertThat(element.getParent(), equalTo(null));
		assertThat(element.isPrototype(), equalTo(true));
		verify(workflowLogic).findProcessClass(eq("123"));
		verify(processStatusHelper).allValues();
		verify(processStatusHelper).defaultValue();
		verifyNoMoreInteractions(errorHandler, workflowLogic, processStatusHelper);
	}
}
