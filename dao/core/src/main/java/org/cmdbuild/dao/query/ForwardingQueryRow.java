package org.cmdbuild.dao.query;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingQueryRow extends ForwardingObject implements CMQueryRow {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingQueryRow() {
	}

	@Override
	public Long getNumber() {
		return delegate().getNumber();
	}

	@Override
	public CMValueSet getValueSet(final Alias alias) {
		return delegate().getValueSet(alias);
	}

	@Override
	public boolean hasCard(Alias alias) {
		return delegate().hasCard(alias);
	}

	@Override
	public CMCard getCard(final Alias alias) {
		return delegate().getCard(alias);
	}

	@Override
	public boolean hasCard(CMClass type) {
		return delegate().hasCard(type);
	}

	@Override
	public CMCard getCard(final CMClass type) {
		return delegate().getCard(type);
	}

	@Override
	public QueryRelation getRelation(final Alias alias) {
		return delegate().getRelation(alias);
	}

	@Override
	public QueryRelation getRelation(final CMDomain type) {
		return delegate().getRelation(type);
	}

	@Override
	protected abstract CMQueryRow delegate();

}
