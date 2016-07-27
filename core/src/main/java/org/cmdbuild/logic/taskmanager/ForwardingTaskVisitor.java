package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTask;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingTaskVisitor extends ForwardingObject implements TaskVisitor {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingTaskVisitor() {
	}

	@Override
	protected abstract TaskVisitor delegate();

	@Override
	public void visit(final AsynchronousEventTask task) {
		delegate().visit(task);
	}

	@Override
	public void visit(final ConnectorTask task) {
		delegate().visit(task);
	}

	@Override
	public void visit(final GenericTask task) {
		delegate().visit(task);
	}

	@Override
	public void visit(final ReadEmailTask task) {
		delegate().visit(task);
	}

	@Override
	public void visit(final StartWorkflowTask task) {
		delegate().visit(task);
	}

	@Override
	public void visit(final SynchronousEventTask task) {
		delegate().visit(task);
	}

}
