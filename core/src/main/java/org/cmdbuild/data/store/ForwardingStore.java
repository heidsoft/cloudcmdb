package org.cmdbuild.data.store;

import java.util.Collection;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingStore<T extends Storable> extends ForwardingObject implements Store<T> {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingStore() {
	}

	@Override
	protected abstract Store<T> delegate();

	@Override
	public Storable create(final T storable) {
		return delegate().create(storable);
	}

	@Override
	public T read(final Storable storable) {
		return delegate().read(storable);
	}

	@Override
	public Collection<T> readAll() {
		return delegate().readAll();
	}

	@Override
	public Collection<T> readAll(final Groupable groupable) {
		return delegate().readAll(groupable);
	}

	@Override
	public void update(final T storable) {
		delegate().update(storable);
	}

	@Override
	public void delete(final Storable storable) {
		delegate().delete(storable);
	}

}
