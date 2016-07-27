package org.cmdbuild.cmdbf.xml;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.ForwardingDomain;

public class CMDomainHistory extends ForwardingDomain {

	private final CMDomain delegate;

	public CMDomainHistory(final CMDomain delegate) {
		this.delegate = delegate;
	}

	@Override
	protected CMDomain delegate() {
		return delegate;
	}

	public CMDomain getBaseType() {
		return delegate;
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return delegate.equals(obj);
	}

}
