package org.cmdbuild.logic.icon;

import org.cmdbuild.logic.icon.Types.ClassType;
import org.cmdbuild.logic.icon.Types.ProcessType;

public interface TypeVisitor {

	void visit(ClassType type);

	void visit(ProcessType type);

}
