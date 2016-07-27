package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entry.CMEntry.CMEntryDefinition;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingEntryDefinition extends ForwardingObject implements CMEntryDefinition {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEntryDefinition() {
	}

	@Override
	protected abstract CMEntryDefinition delegate();

	@Override
	public CMEntryDefinition set(final String key, final Object value) {
		delegate().set(key, value);
		return this;
	}

	@Override
	public CMEntryDefinition setUser(final String user) {
		delegate().setUser(user);
		return this;
	}

	@Override
	public CMEntry save() {
		return delegate().save();
	}

}
