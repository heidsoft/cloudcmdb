package unit.logic.taskmanager.scheduler;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.scheduler.DefaultSchedulerFacade;
import org.cmdbuild.logic.taskmanager.scheduler.LogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.scheduler.LogicAndSchedulerConverter.LogicAsSourceConverter;
import org.cmdbuild.logic.taskmanager.scheduler.SchedulerFacade.Callback;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class DefaultSchedulerFacadeTest {

	private SchedulerService schedulerService;
	private LogicAndSchedulerConverter converter;

	private DefaultSchedulerFacade schedulerFacade;
	private Callback callback;

	@Before
	public void setUp() throws Exception {
		schedulerService = mock(SchedulerService.class);
		converter = mock(LogicAndSchedulerConverter.class);
		schedulerFacade = new DefaultSchedulerFacade(schedulerService, converter);
		callback = mock(Callback.class);
	}

	@Test
	public void scheduleCreatedOnlyIfTaskIsActive() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.build();

		// when
		schedulerFacade.create(task, callback);

		// then
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduleCreated() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.build();
		final Job job = mock(Job.class);
		final LogicAsSourceConverter logicAsSourceConverter = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toJob()) //
				.thenReturn(job);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverter);

		// when
		schedulerFacade.create(task, callback);

		// then
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verify(converter).from(task);
		inOrder.verify(schedulerService).add(any(Job.class), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedTrigger, instanceOf(RecurringTrigger.class));
		assertThat(RecurringTrigger.class.cast(capturedTrigger).getCronExpression(), endsWith("cron expression"));
	}

	@Test
	public void secondsAddedToSpecifiedCronExpression() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(true) //
				.withCronExpression("<actual cron expression>") //
				.build();
		final Job job = mock(Job.class);
		final LogicAsSourceConverter logicAsSourceConverter = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toJob()) //
				.thenReturn(job);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverter);

		// when
		schedulerFacade.create(task, callback);

		// then
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verify(converter).from(task);
		inOrder.verify(schedulerService).add(any(Job.class), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedTrigger, instanceOf(RecurringTrigger.class));
		assertThat(RecurringTrigger.class.cast(capturedTrigger).getCronExpression(),
				equalTo("0 <actual cron expression>"));
	}

	@Test
	public void scheduleDeletedOnlyIfTaskIsNotActive() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(true) //
				.build();

		// when
		schedulerFacade.delete(task);

		// then
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduleDeleted() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(false) //
				.build();
		final Job job = mock(Job.class);
		final LogicAsSourceConverter logicAsSourceConverterWithNoExecution = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverterWithNoExecution.toJob()) //
				.thenReturn(job);
		final LogicAsSourceConverter logicAsSourceConverterWithExecution = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverterWithExecution.withNoExecution()) //
				.thenReturn(logicAsSourceConverterWithNoExecution);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverterWithExecution);

		// when
		schedulerFacade.delete(task);

		// then
		final InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verify(converter).from(task);
		inOrder.verify(schedulerService).remove(job);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void callbackUsedWhenJobIsExecuted() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.build();
		final Job job = mock(Job.class);
		final LogicAsSourceConverter logicAsSourceConverter = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toJob()) //
				.thenReturn(job);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverter);

		// when
		schedulerFacade.create(task, callback);

		// then
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verify(converter).from(task);
		inOrder.verify(schedulerService).add(jobCaptor.capture(), any(Trigger.class));
		inOrder.verifyNoMoreInteractions();

		// and given
		final Job capturedJob = jobCaptor.getValue();

		// when
		capturedJob.execute();

		// then

		inOrder = inOrder(callback);
		inOrder.verify(callback).start();
		inOrder.verify(callback).stop();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void callbackUsedWhenJobThrowsAnException() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.build();
		final Throwable EXCEPTION = new RuntimeException();
		final Job job = mock(Job.class);
		doThrow(EXCEPTION).when(job).execute();
		final LogicAsSourceConverter logicAsSourceConverter = mock(LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toJob()) //
				.thenReturn(job);
		when(converter.from(task)) //
				.thenReturn(logicAsSourceConverter);

		// when
		schedulerFacade.create(task, callback);

		// then
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		InOrder inOrder = inOrder(schedulerService, converter);
		inOrder.verify(converter).from(task);
		inOrder.verify(schedulerService).add(jobCaptor.capture(), any(Trigger.class));
		inOrder.verifyNoMoreInteractions();

		// and given
		final Job capturedJob = jobCaptor.getValue();

		// when
		try {
			capturedJob.execute();
		} catch (final Throwable e) {
			// forgive
		}

		// then

		inOrder = inOrder(callback);
		inOrder.verify(callback).start();
		inOrder.verify(callback).error(EXCEPTION);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduleExecuted() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.build();
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn("foo");
		final LogicAsSourceConverter toJobConverter = mock(LogicAsSourceConverter.class);
		when(toJobConverter.toJob()) //
				.thenReturn(job);
		when(converter.from(task)) //
				.thenReturn(toJobConverter);

		// when
		schedulerFacade.execute(task, callback);

		// then
		final InOrder inOrder = inOrder(schedulerService, converter, job);
		inOrder.verify(converter).from(task);
		inOrder.verify(job).execute();
		inOrder.verify(job).getName();
		inOrder.verifyNoMoreInteractions();
	}

}
