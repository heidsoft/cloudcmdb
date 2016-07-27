package org.cmdbuild.logic.taskmanager.scheduler;

import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.scheduler.Job;

public interface JobFactory<T extends ScheduledTask> {

	Job create(ScheduledTask task, boolean execution);

}