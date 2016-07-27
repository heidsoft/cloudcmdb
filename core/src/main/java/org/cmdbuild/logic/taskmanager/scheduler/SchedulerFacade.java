package org.cmdbuild.logic.taskmanager.scheduler;

import org.cmdbuild.logic.taskmanager.ScheduledTask;

public interface SchedulerFacade {

	interface Callback {

		void start();

		void stop();

		void error(Throwable e);

	}

	/**
	 * Creates a new {@link ScheduledTask}.
	 */
	void create(ScheduledTask task, Callback callback);

	/**
	 * Deletes an existing {@link ScheduledTask}.
	 */
	void delete(ScheduledTask task);

	/**
	 * Execute an existing {@link ScheduledTask}.
	 */
	void execute(ScheduledTask task, Callback callback);

}
