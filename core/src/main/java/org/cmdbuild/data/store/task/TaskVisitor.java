package org.cmdbuild.data.store.task;

public interface TaskVisitor {

	void visit(AsynchronousEventTask task);

	void visit(ConnectorTask task);

	void visit(GenericTask task);

	void visit(ReadEmailTask task);

	void visit(StartWorkflowTask task);

	void visit(SynchronousEventTask task);

}
