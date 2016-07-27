package org.cmdbuild.logic.data.access.filter.model;

public class Contains extends AbstractPredicate {

	private final Object value;

	Contains(final Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
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
		if (!(obj instanceof Contains)) {
			return false;
		}
		final Contains other = Contains.class.cast(obj);
		return this.value.equals(other.value);
	}

	@Override
	protected int doHashCode() {
		return value.hashCode();
	}

}
