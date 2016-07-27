package org.cmdbuild.service.rest.v2.cxf.filter;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;

public abstract class ForwardingPredicate<T> extends ForwardingObject implements Predicate<T> {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingPredicate() {
	}

	@Override
	protected abstract Predicate<T> delegate();

	@Override
	public boolean apply(final T input) {
		return delegate().apply(input);
	}

}
