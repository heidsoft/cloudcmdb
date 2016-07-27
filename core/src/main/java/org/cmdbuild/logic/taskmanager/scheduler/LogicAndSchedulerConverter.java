package org.cmdbuild.logic.taskmanager.scheduler;

import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.scheduler.Job;

public interface LogicAndSchedulerConverter {

	interface LogicAsSourceConverter {

		LogicAsSourceConverter withNoExecution();

		Job toJob();

	}

	LogicAsSourceConverter from(ScheduledTask source);

}