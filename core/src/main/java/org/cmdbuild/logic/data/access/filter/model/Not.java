package org.cmdbuild.logic.data.access.filter.model;

public class Not extends AbstractPredicate {

	private final Predicate predicate;

	Not(final Predicate predicate) {
		this.predicate = predicate;
	}

	public Predicate getPredicate() {
		return predicate;
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
		if (!(obj instanceof Not)) {
			return false;
		}
		final Not other = Not.class.cast(obj);
		return this.predicate.equals(other.predicate);
	}

	@Override
	protected int doHashCode() {
		return predicate.hashCode();
	}

}
