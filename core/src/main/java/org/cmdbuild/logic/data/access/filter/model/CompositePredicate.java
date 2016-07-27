package org.cmdbuild.logic.data.access.filter.model;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Iterator;

abstract class CompositePredicate extends AbstractPredicate implements Iterable<Predicate> {

	private final Iterable<Predicate> predicates;

	protected CompositePredicate(final Iterable<Predicate> elements) {
		this.predicates = newHashSet(elements);
	}

	@Override
	public Iterator<Predicate> iterator() {
		return predicates.iterator();
	}

	public Iterable<Predicate> getPredicates() {
		return predicates;
	}

	@Override
	protected int doHashCode() {
		return getPredicates().hashCode();
	}

}
