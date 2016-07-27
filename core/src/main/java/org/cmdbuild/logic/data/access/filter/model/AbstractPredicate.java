package org.cmdbuild.logic.data.access.filter.model;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

abstract class AbstractPredicate implements Predicate {

	protected AbstractPredicate() {
		// usable by subclasses only
	}

	@Override
	public final boolean equals(final Object obj) {
		return doEquals(obj);
	}

	protected abstract boolean doEquals(Object obj);

	@Override
	public final int hashCode() {
		return doHashCode();
	}

	protected abstract int doHashCode();

	@Override
	public final String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
