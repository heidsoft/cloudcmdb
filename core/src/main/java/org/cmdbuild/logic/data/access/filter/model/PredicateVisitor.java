package org.cmdbuild.logic.data.access.filter.model;

public interface PredicateVisitor {

	void visit(And predicate);

	void visit(Contains predicate);

	void visit(EndsWith predicate);

	void visit(EqualTo predicate);

	void visit(GreaterThan predicate);

	void visit(In predicate);

	void visit(IsNull predicate);

	void visit(LessThan predicate);

	void visit(Like predicate);

	void visit(Not predicate);

	void visit(Or predicate);

	void visit(StartsWith predicate);

}
