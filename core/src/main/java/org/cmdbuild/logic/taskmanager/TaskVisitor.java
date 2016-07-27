package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTask;

public interface TaskVisitor {

	void visit(AsynchronousEventTask task);

	void visit(ConnectorTask task);

	void visit(GenericTask task);

	void visit(ReadEmailTask task);

	void visit(StartWorkflowTask task);

	void visit(SynchronousEventTask task);

}
