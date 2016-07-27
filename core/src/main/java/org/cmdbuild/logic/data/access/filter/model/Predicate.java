package org.cmdbuild.logic.data.access.filter.model;

public interface Predicate {

	void accept(PredicateVisitor visitor);

}
