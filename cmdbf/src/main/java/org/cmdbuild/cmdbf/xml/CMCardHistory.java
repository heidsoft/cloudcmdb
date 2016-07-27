package org.cmdbuild.cmdbf.xml;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.dao.entrytype.CMClass;

public class CMCardHistory extends ForwardingCard {

	private final CMCard delegate;
	private final CMClassHistory type;

	public CMCardHistory(final CMCard delegate) {
		this.delegate = delegate;
		this.type = new CMClassHistory(delegate.getType());
	}

	@Override
	protected CMCard delegate() {
		return delegate;
	}

	@Override
	public Long getId() {
		return getCurrentId();
	}

	@Override
	public CMClass getType() {
		return type;
	}
}
