package org.cmdbuild.data.store;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingGroupable extends ForwardingObject implements Groupable {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingGroupable() {
	}

	@Override
	protected abstract Groupable delegate();

	@Override
	public String getGroupAttributeName() {
		return delegate().getGroupAttributeName();
	}

	@Override
	public Object getGroupAttributeValue() {
		return delegate().getGroupAttributeValue();
	}

}
