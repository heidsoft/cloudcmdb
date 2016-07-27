package org.cmdbuild.data.store;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * In-memory implementation of a {@link Store}.
 */
public class InMemoryStore<T extends Storable> implements Store<T> {

	public static <T extends Storable> InMemoryStore<T> of(final Class<T> type) {
		return new InMemoryStore<T>();
	}

	private final Map<String, T> map;

	private InMemoryStore() {
		map = newHashMap();
	}

	@Override
	public Storable create(final T storable) {
		map.put(storable.getIdentifier(), storable);
		return storable;
	}

	@Override
	public T read(final Storable storable) {
		for (final Entry<String, T> entry : map.entrySet()) {
			if (entry.getKey().equals(storable.getIdentifier())) {
				return entry.getValue();
			}
		}
		throw new NoSuchElementException();
	}

	@Override
	public Collection<T> readAll() {
		return map.values();
	}

	@Override
	public Collection<T> readAll(final Groupable groupable) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void update(final T storable) {
		map.put(storable.getIdentifier(), storable);
	}

	@Override
	public void delete(final Storable storable) {
		map.remove(storable.getIdentifier());
	}

}
