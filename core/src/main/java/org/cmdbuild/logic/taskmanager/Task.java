package org.cmdbuild.logic.taskmanager;

public interface Task {

	void accept(TaskVisitor visitor);

	Long getId();

	String getDescription();

	boolean isActive();

	boolean isExecutable();

}
