package org.cmdbuild.cmdbf.cmdbmdr;

import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.ForwardingEntry;
import org.cmdbuild.dao.entrytype.CMDomain;

public class CmdbRelation extends ForwardingEntry implements CMRelation {

	private final CMRelation delegate;
	private final String card1ClassName;
	private final String card2ClassName;

	public CmdbRelation(final CMRelation delegate, final String card1ClassName, final String card2ClassName) {
		this.delegate = delegate;
		this.card1ClassName = card1ClassName;
		this.card2ClassName = card2ClassName;
	}

	@Override
	protected CMEntry delegate() {
		return delegate;
	}

	@Override
	public CMDomain getType() {
		return delegate.getType();
	}

	@Override
	public Long getCard1Id() {
		return delegate.getCard1Id();
	}

	@Override
	public Long getCard2Id() {
		return delegate.getCard2Id();
	}

	public String getCard1ClassName() {
		return card1ClassName;
	}

	public String getCard2ClassName() {
		return card2ClassName;
	}
}
