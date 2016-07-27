package org.cmdbuild.service.rest.v2.model;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class AbstractModel implements Model {

	protected AbstractModel() {
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
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}