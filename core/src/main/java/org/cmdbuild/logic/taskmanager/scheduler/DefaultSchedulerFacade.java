package org.cmdbuild.logic.taskmanager.scheduler;

import static com.google.common.base.Throwables.propagate;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.scheduler.ForwardingJob;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultSchedulerFacade implements SchedulerFacade {

	private static final Logger logger = Logic.logger;
	private static final Marker MARKER = MarkerFactory.getMarker(DefaultSchedulerFacade.class.getName());

	private static class SuppressedExceptionJob extends ForwardingJob {

		private final Job delegate;

		private SuppressedExceptionJob(final Job delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Job delegate() {
			return delegate;
		}

		@Override
		public void execute() {
			try {
				delegate().execute();
			} catch (final Throwable e) {
				logger.warn("error executing job", e);
			}
		}

	}

	private static class JobWithCallback extends ForwardingJob {

		private final Job delegate;
		private final Callback callback;

		public JobWithCallback(final Job delegate, final Callback callback) {
			this.delegate = delegate;
			this.callback = callback;
		}

		@Override
		protected Job delegate() {
			return delegate;
		}

		@Override
		public void execute() {
			callback.start();
			try {
				super.execute();
			} catch (final Throwable e) {
				callback.error(e);
				propagate(e);
			}
			callback.stop();
		}

	}

	private final LogicAndSchedulerConverter converter;
	private final SchedulerService schedulerService;

	public DefaultSchedulerFacade(final SchedulerService schedulerService, final LogicAndSchedulerConverter converter) {
		this.converter = converter;
		this.schedulerService = schedulerService;
	}

	@Override
	public void create(final ScheduledTask task, final Callback callback) {
		logger.info(MARKER, "creating a new scheduled task '{}'", task);
		if (task.isActive()) {
			final Job job = new SuppressedExceptionJob(jobFrom(task, callback));
			final Trigger trigger = RecurringTrigger.at(addSecondsField(task.getCronExpression()));
			schedulerService.add(job, trigger);
		}
	}

	private String addSecondsField(final String cronExpression) {
		return "0 " + cronExpression;
	}

	@Override
	public void delete(final ScheduledTask task) {
		logger.info(MARKER, "deleting an existing scheduled task '{}'", task);
		if (!task.isActive()) {
			final Job job = converter.from(task).withNoExecution().toJob();
			schedulerService.remove(job);
		}
	}

	@Override
	public void execute(final ScheduledTask task, final Callback callback) {
		logger.info(MARKER, "executing an existing scheduled task '{}'", task);
		jobFrom(task, callback).execute();
	}

	private Job jobFrom(final ScheduledTask task, final Callback callback) {
		final Job job = converter.from(task).toJob();
		final Job jobWithCallback = new JobWithCallback(job, callback);
		final Job jobWithLogging = new JobWithCallback(jobWithCallback, LoggingCallback.of(jobWithCallback));
		return jobWithLogging;
	}

}
