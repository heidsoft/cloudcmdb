package org.cmdbuild.logic.data.access.filter.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class IsNull extends AbstractPredicate {

	private final int hashCode;

	IsNull() {
		hashCode = HashCodeBuilder.reflectionHashCode(this);
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
		if (!(obj instanceof IsNull)) {
			return false;
		}
		return true;
	}

	@Override
	protected int doHashCode() {
		return hashCode;
	}

}
