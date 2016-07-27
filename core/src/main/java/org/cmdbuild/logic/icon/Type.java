package org.cmdbuild.logic.icon;

public interface Type {

	void accept(TypeVisitor visitor);

}
