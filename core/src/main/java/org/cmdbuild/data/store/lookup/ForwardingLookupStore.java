package org.cmdbuild.data.store.lookup;

import org.cmdbuild.data.store.ForwardingStore;

public abstract class ForwardingLookupStore extends ForwardingStore<Lookup> implements LookupStore {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingLookupStore() {
	}

	@Override
	protected abstract LookupStore delegate();

	@Override
	public Iterable<Lookup> readAll(final LookupType type) {
		return delegate().readAll(type);
	}

	@Override
	public Iterable<LookupType> readAllTypes() {
		return delegate().readAllTypes();
	}

	@Override
	public Iterable<Lookup> readFromUuid(final String uuid) {
		return delegate().readFromUuid(uuid);
	}

}
