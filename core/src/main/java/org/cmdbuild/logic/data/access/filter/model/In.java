package org.cmdbuild.logic.data.access.filter.model;

import static com.google.common.collect.Sets.newHashSet;

public class In extends AbstractPredicate {

	private final Iterable<Object> values;

	In(final Iterable<? extends Object> values) {
		this.values = newHashSet(values);
	}

	public Iterable<Object> getValues() {
		return values;
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
		if (!(obj instanceof In)) {
			return false;
		}
		final In other = In.class.cast(obj);
		return this.values.equals(other.values);
	}

	@Override
	protected int doHashCode() {
		return values.hashCode();
	}

}
