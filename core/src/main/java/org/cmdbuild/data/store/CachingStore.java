package org.cmdbuild.data.store;

import java.util.Collection;
import java.util.NoSuchElementException;

import com.google.common.cache.Cache;

public abstract class CachingStore<T extends Storable> implements Store<T> {

	protected abstract Cache<String, T> delegate();

	@Override
	public Storable create(final T storable) {
		delegate().put(storable.getIdentifier(), storable);
		return storable;
	}

	@Override
	public T read(final Storable storable) {
		final T found = delegate().getIfPresent(storable.getIdentifier());
		if (found == null) {
			throw new NoSuchElementException();
		}
		return found;
	}

	@Override
	public Collection<T> readAll() {
		return delegate().asMap().values();
	}

	@Override
	public Collection<T> readAll(final Groupable groupable) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void update(final T storable) {
		final T found = delegate().getIfPresent(storable.getIdentifier());
		if (found == null) {
			throw new NoSuchElementException();
		}
		delegate().put(storable.getIdentifier(), storable);
	}

	@Override
	public void delete(final Storable storable) {
		final T found = delegate().getIfPresent(storable.getIdentifier());
		if (found == null) {
			throw new NoSuchElementException();
		}
		delegate().invalidate(storable.getIdentifier());
	}

}
