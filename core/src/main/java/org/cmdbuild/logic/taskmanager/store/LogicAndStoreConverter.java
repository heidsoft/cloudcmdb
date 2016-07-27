package org.cmdbuild.logic.taskmanager.store;

import org.cmdbuild.logic.taskmanager.Task;

public interface LogicAndStoreConverter {

	interface LogicAsSourceConverter {

		org.cmdbuild.data.store.task.Task toStore();

	}

	interface StoreAsSourceConverter {

		Task toLogic();

	}

	LogicAsSourceConverter from(Task source);

	StoreAsSourceConverter from(org.cmdbuild.data.store.task.Task source);

}
