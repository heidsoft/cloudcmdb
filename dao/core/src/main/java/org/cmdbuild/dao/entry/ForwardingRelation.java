package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMDomain;

public abstract class ForwardingRelation extends ForwardingEntry implements CMRelation {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingRelation() {
	}

	@Override
	protected abstract CMRelation delegate();

	@Override
	public CMDomain getType() {
		return delegate().getType();
	}

	@Override
	public Long getCard1Id() {
		return delegate().getCard1Id();
	}

	@Override
	public Long getCard2Id() {
		return delegate().getCard2Id();
	}

}
