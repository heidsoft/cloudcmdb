package unit.logic.taskmanager.scheduler;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.command.Command;
import org.junit.Test;

public class AbstractJobFactoryTest {

	@Test
	public void commandNotUsedWhenExecutionIsNotRequired() {
		// given
		final AbstractJobFactory<ScheduledTask> underTest = new AbstractJobFactory<ScheduledTask>() {

			@Override
			protected Class<ScheduledTask> getType() {
				return ScheduledTask.class;
			}

			@Override
			protected Command command(final ScheduledTask task) {
				throw new UnsupportedOperationException("should not be called");
			}

		};
		final ScheduledTask task = mock(ScheduledTask.class);
		doReturn(42L) //
				.when(task).getId();

		// when
		final Job output = underTest.create(task, false);
		output.execute();

		// then
		verify(task).getId();
		verifyNoMoreInteractions(task);
		assertThat(output.getName(), equalTo("42"));
	}

	@Test
	public void commandUsedWhenExecutionIsRequired() {
		// given
		final Command command = mock(Command.class);
		final AbstractJobFactory<ScheduledTask> underTest = new AbstractJobFactory<ScheduledTask>() {

			@Override
			protected Class<ScheduledTask> getType() {
				return ScheduledTask.class;
			}

			@Override
			protected Command command(final ScheduledTask task) {
				return command;
			}

		};
		final ScheduledTask task = mock(ScheduledTask.class);
		doReturn(42L) //
				.when(task).getId();

		// when
		final Job output = underTest.create(task, true);
		output.execute();

		// then
		verify(task).getId();
		verify(command).execute();
		verifyNoMoreInteractions(task, command);
		assertThat(output.getName(), equalTo("42"));
	}

}
