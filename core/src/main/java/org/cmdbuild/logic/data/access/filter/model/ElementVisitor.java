package org.cmdbuild.logic.data.access.filter.model;

public interface ElementVisitor {

	void visit(All element);

	void visit(Attribute element);

	void visit(OneOf element);

}
