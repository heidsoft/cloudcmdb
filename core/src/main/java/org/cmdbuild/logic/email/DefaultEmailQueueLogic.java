package org.cmdbuild.logic.email;

import static org.cmdbuild.scheduler.Triggers.everyMinute;
import static org.cmdbuild.scheduler.command.Commands.safe;
import static org.joda.time.DateTime.now;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.command.BuildableCommandBasedJob;
import org.cmdbuild.scheduler.command.Command;
import org.joda.time.DateTime;

public class DefaultEmailQueueLogic implements EmailQueueLogic {

	private static class AdvancedCommand implements Command {

		private final Command delegate;
		private final EmailConfiguration configuration;

		private final AtomicBoolean running = new AtomicBoolean(false);
		private DateTime lastExecution;

		public AdvancedCommand(final Command delegate, final EmailConfiguration configuration) {
			this.delegate = delegate;
			this.configuration = configuration;
		}

		@Override
		public void execute() {
			if (!running.getAndSet(true)) {
				if ((lastExecution == null) || now().isAfter(lastExecution.plus(configuration.getQueueTime()))) {
					lastExecution = now();
					safe(delegate).execute();
				} else {
					logger.debug("time not elapsed");
				}
				running.set(false);
			} else {
				logger.debug("queue already running");
			}
		}

	}

	private final EmailConfiguration configuration;
	private final SchedulerService schedulerService;
	private final Job job;

	public DefaultEmailQueueLogic(final EmailConfiguration configuration, final SchedulerService schedulerService,
			final Command command) {
		this.configuration = configuration;
		this.schedulerService = schedulerService;
		this.job = BuildableCommandBasedJob.newInstance() //
				.withName(DefaultEmailQueueLogic.class.getName()) //
				.withCommand(new AdvancedCommand(command, configuration)) //
				.build();
	}

	@Override
	public boolean running() {
		return schedulerService.isStarted(job);
	}

	@Override
	public void start() {
		configuration.setEnabled(true);
		configuration.save();
		schedulerService.add(job, everyMinute());
	}

	@Override
	public void stop() {
		if (running()) {
			schedulerService.remove(job);
			configuration.setEnabled(false);
			configuration.save();
		}
	}

	@Override
	public Configuration configuration() {
		return new Configuration() {

			@Override
			public long time() {
				return configuration.getQueueTime();
			}

		};
	}

	@Override
	public void configure(final Configuration configuration) {
		validate(configuration);
		this.configuration.setQueueTime(configuration.time());
		this.configuration.save();
	}

	private void validate(final Configuration configuration) {
		Validate.notNull(configuration, "missing configuration");
		Validate.isTrue(configuration.time() >= 0, "invalid time");
	}

}
