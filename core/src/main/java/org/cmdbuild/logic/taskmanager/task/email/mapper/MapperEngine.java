package org.cmdbuild.logic.taskmanager.task.email.mapper;

public interface MapperEngine {

	void accept(MapperEngineVisitor visitor);

}
