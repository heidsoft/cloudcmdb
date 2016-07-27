package org.cmdbuild.logic.taskmanager.event;

import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.services.event.Observer;

public interface LogicAndObserverConverter {

	interface LogicAsSourceConverter {

		Observer toObserver();

	}

	LogicAsSourceConverter from(SynchronousEventTask source);

}