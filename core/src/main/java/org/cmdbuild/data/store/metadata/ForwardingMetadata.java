package org.cmdbuild.data.store.metadata;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingMetadata extends ForwardingObject implements Metadata {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingMetadata() {
	}

	@Override
	protected abstract Metadata delegate();

	@Override
	public String getIdentifier() {
		return delegate().getIdentifier();
	}

	@Override
	public String name() {
		return delegate().name();
	}

	@Override
	public String value() {
		return delegate().value();
	}

}
