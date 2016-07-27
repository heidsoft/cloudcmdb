package org.cmdbuild.logic.mapping;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingFilterMapper extends ForwardingObject implements FilterMapper {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingFilterMapper() {
	}

	@Override
	protected abstract FilterMapper delegate();

	@Override
	public CMEntryType entryType() {
		return delegate().entryType();
	}

	@Override
	public Iterable<WhereClause> whereClauses() {
		return delegate().whereClauses();
	}

	@Override
	public Iterable<JoinElement> joinElements() {
		return delegate().joinElements();
	}

}
