package org.cmdbuild.logic.taskmanager.event;

import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.services.event.Observer;

public interface ObserverFactory {

	public Observer create(SynchronousEventTask task);

}