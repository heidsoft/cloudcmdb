package org.cmdbuild.logic.taskmanager.scheduler;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.taskmanager.scheduler.SchedulerFacade.Callback;
import org.cmdbuild.scheduler.Job;
import org.slf4j.Logger;

public class LoggingCallback implements Callback {

	private static final Logger logger = Log.CMDBUILD;

	public static LoggingCallback of(final Job job) {
		return new LoggingCallback(job);
	}

	private final Job job;

	private LoggingCallback(final Job job) {
		this.job = job;
	}

	@Override
	public void start() {
		logger.info("starting job '{}'", job.getName());
	}

	@Override
	public void stop() {
		logger.info("stopping job '{}'", job.getName());
	}

	@Override
	public void error(final Throwable e) {
		final String message = String.format("error on job '%s'", job.getName());
		logger.error(message, e);
	}

}
