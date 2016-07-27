package org.cmdbuild.logic.taskmanager.task.email.mapper;

public interface MapperEngineVisitor {

	void visit(KeyValueMapperEngine mapper);

	void visit(NullMapperEngine mapper);

}
