package org.cmdbuild.cmdbf.xml;

import org.cmdbuild.cmdbf.cmdbmdr.CmdbRelation;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMDomain;

public class CMRelationHistory extends CmdbRelation {

	private final CMDomainHistory type;

	public CMRelationHistory(final CMRelation delegate, final String card1ClassName, final String card2ClassName) {
		super(delegate, card1ClassName, card2ClassName);
		this.type = new CMDomainHistory(delegate.getType());
	}

	@Override
	public CMDomain getType() {
		return type;
	}
}
