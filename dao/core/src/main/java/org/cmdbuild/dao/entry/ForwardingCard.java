package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMClass;

public abstract class ForwardingCard extends ForwardingEntry implements CMCard {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingCard() {
	}

	@Override
	protected abstract CMCard delegate();

	@Override
	public CMClass getType() {
		return delegate().getType();
	}

	@Override
	public Object getCode() {
		return delegate().getCode();
	}

	@Override
	public Object getDescription() {
		return delegate().getDescription();
	}

	@Override
	public Long getCurrentId() {
		return delegate().getCurrentId();
	}

}
