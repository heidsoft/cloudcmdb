package org.cmdbuild.logic.data.access.filter.model;

import static com.google.common.collect.Iterables.elementsEqual;

public class Or extends CompositePredicate {

	Or(final Iterable<Predicate> predicates) {
		super(predicates);
	}

	@Override
	public void accept(final PredicateVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Or)) {
			return false;
		}
		final Or other = Or.class.cast(obj);
		return elementsEqual(this.getPredicates(), other.getPredicates());
	}

}