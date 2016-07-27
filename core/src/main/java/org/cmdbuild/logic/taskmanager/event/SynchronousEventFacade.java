package org.cmdbuild.logic.taskmanager.event;

import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;

public interface SynchronousEventFacade {

	void create(SynchronousEventTask task);

	void delete(SynchronousEventTask task);

}
