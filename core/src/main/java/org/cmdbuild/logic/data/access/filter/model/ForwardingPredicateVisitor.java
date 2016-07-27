package org.cmdbuild.logic.data.access.filter.model;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingPredicateVisitor extends ForwardingObject implements PredicateVisitor {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingPredicateVisitor() {
	}

	@Override
	protected abstract PredicateVisitor delegate();

	@Override
	public void visit(final And predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final Contains predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final EndsWith predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final EqualTo predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final GreaterThan predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final In predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final IsNull predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final LessThan predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final Like predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final Not predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final Or predicate) {
		delegate().visit(predicate);
	}

	@Override
	public void visit(final StartsWith predicate) {
		delegate().visit(predicate);
	}

}
