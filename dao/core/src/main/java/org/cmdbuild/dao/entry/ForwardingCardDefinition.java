package org.cmdbuild.dao.entry;

import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMEntry.CMEntryDefinition;

public abstract class ForwardingCardDefinition extends ForwardingEntryDefinition implements CMCardDefinition {

	private final CMCardDefinition delegate;

	protected ForwardingCardDefinition(final CMCardDefinition delegate) {
		this.delegate = delegate;
	}

	@Override
	protected CMEntryDefinition delegate() {
		return delegate;
	}

	@Override
	public CMCardDefinition set(final String key, final Object value) {
		delegate.set(key, value);
		return this;
	}

	@Override
	public CMCardDefinition set(final Iterable<? extends Entry<String, ? extends Object>> keysAndValues) {
		delegate.set(keysAndValues);
		return this;
	}

	@Override
	public CMCardDefinition setUser(final String user) {
		delegate.setUser(user);
		return this;
	}

	@Override
	public CMCardDefinition setCode(final Object value) {
		delegate.setCode(value);
		return this;
	}

	@Override
	public CMCardDefinition setDescription(final Object value) {
		delegate.setDescription(value);
		return this;
	}

	@Override
	public CMCardDefinition setCurrentId(final Long currentId) {
		delegate.setCurrentId(currentId);
		return this;
	}

	@Override
	public CMCard save() {
		return delegate.save();
	}

}
