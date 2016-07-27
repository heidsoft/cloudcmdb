package org.cmdbuild.dao.query;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingQueryResult extends ForwardingObject implements CMQueryResult {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingQueryResult() {
	}

	@Override
	public Iterator<CMQueryRow> iterator() {
		return delegate().iterator();
	}

	@Override
	public int size() {
		return delegate().size();
	}

	@Override
	public boolean isEmpty() {
		return delegate().isEmpty();
	}

	@Override
	public int totalSize() {
		return delegate().totalSize();
	}

	@Override
	public CMQueryRow getOnlyRow() throws NoSuchElementException {
		return delegate().getOnlyRow();
	}

	@Override
	protected abstract CMQueryResult delegate();
}
