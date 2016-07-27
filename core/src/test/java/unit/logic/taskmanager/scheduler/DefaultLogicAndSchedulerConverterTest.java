package unit.logic.taskmanager.scheduler;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.taskmanager.scheduler.DefaultLogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.scheduler.JobFactory;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTask;
import org.cmdbuild.scheduler.Job;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DefaultLogicAndSchedulerConverterTest {

	private DefaultLogicAndSchedulerConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new DefaultLogicAndSchedulerConverter();
	}

	@Test
	public void connectorTaskSuccessfullyConvertedToJob() throws Exception {
		// given
		final ConnectorTask task = ConnectorTask.newInstance().build();

		final Job job = mock(Job.class);
		final JobFactory<ConnectorTask> factory = mock(JobFactory.class);
		when(factory.create(task, true)) //
				.thenReturn(job);
		converter.register(ConnectorTask.class, factory);

		// when
		converter.from(task).toJob();

		// then
		final InOrder inOrder = inOrder(factory);
		inOrder.verify(factory).create(task, true);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void genericTaskSuccessfullyConvertedToJob() throws Exception {
		// given
		final GenericTask task = GenericTask.newInstance().build();

		final Job job = mock(Job.class);
		final JobFactory<GenericTask> factory = mock(JobFactory.class);
		when(factory.create(task, true)) //
				.thenReturn(job);
		converter.register(GenericTask.class, factory);

		// when
		converter.from(task).toJob();

		// then
		final InOrder inOrder = inOrder(factory);
		inOrder.verify(factory).create(task, true);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToJob() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance().build();

		final Job job = mock(Job.class);
		final JobFactory<ReadEmailTask> factory = mock(JobFactory.class);
		when(factory.create(task, true)) //
				.thenReturn(job);
		converter.register(ReadEmailTask.class, factory);

		// when
		converter.from(task).toJob();

		// then
		final InOrder inOrder = inOrder(factory);
		inOrder.verify(factory).create(task, true);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToJob() throws Exception {
		// given
		final StartWorkflowTask task = StartWorkflowTask.newInstance().build();

		final Job job = mock(Job.class);
		final JobFactory<StartWorkflowTask> factory = mock(JobFactory.class);
		when(factory.create(task, true)) //
				.thenReturn(job);
		converter.register(StartWorkflowTask.class, factory);

		// when
		converter.from(task).toJob();

		// then
		final InOrder inOrder = inOrder(factory);
		inOrder.verify(factory).create(task, true);
		inOrder.verifyNoMoreInteractions();
	}

}
