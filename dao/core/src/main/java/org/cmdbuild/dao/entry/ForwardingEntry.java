package org.cmdbuild.dao.entry;

import java.util.Map.Entry;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.joda.time.DateTime;

public abstract class ForwardingEntry extends ForwardingValueSet implements CMEntry {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEntry() {
	}

	@Override
	protected abstract CMEntry delegate();

	@Override
	public CMEntryType getType() {
		return delegate().getType();
	}

	@Override
	public Long getId() {
		return delegate().getId();
	}

	@Override
	public String getUser() {
		return delegate().getUser();
	}

	@Override
	public DateTime getBeginDate() {
		return delegate().getBeginDate();
	}

	@Override
	public DateTime getEndDate() {
		return delegate().getEndDate();
	}

	@Override
	public Iterable<Entry<String, Object>> getAllValues() {
		return delegate().getAllValues();
	}

}
