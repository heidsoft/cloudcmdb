package org.cmdbuild.logic.data.access.filter.model;

public class Like extends AbstractPredicate {

	private final Object value;

	Like(final Object value) {
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
		if (!(obj instanceof Like)) {
			return false;
		}
		final Like other = Like.class.cast(obj);
		return this.value.equals(other.value);
	}

	@Override
	protected int doHashCode() {
		return value.hashCode();
	}

}
