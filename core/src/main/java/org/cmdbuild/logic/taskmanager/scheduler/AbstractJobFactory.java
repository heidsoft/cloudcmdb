package org.cmdbuild.logic.taskmanager.scheduler;

import static org.cmdbuild.scheduler.command.Commands.nullCommand;

import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.command.BuildableCommandBasedJob;
import org.cmdbuild.scheduler.command.Command;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public abstract class AbstractJobFactory<T extends ScheduledTask> implements JobFactory<T> {

	protected static final Logger logger = DefaultLogicAndSchedulerConverter.logger;
	protected static final Marker marker = MarkerFactory.getMarker(AbstractJobFactory.class.getName());

	protected abstract Class<T> getType();

	@Override
	public final Job create(final ScheduledTask task, final boolean execution) {
		final T specificTask = getType().cast(task);
		return BuildableCommandBasedJob.newInstance() //
				.withName(name(specificTask)) //
				.withCommand(execution ? command(specificTask) : nullCommand()) //
				.build();
	}

	private String name(final T task) {
		return task.getId().toString();
	}

	protected abstract Command command(T task);

}