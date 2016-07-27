package org.cmdbuild.logic.data.access.lock;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Map;

import org.cmdbuild.logic.data.access.lock.LockableStore.Lock;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingMap;

public class MapLockableStore<M extends Lock> extends ForwardingMap<Lockable, M> implements LockableStore<M> {

	private final Map<Lockable, M> delegate;

	public MapLockableStore(final Map<Lockable, M> delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Map<Lockable, M> delegate() {
		return delegate;
	}

	@Override
	public void add(final Lockable lockable, final M lock) {
		delegate().put(lockable, lock);
	}

	@Override
	public void remove(final Lockable lockable) {
		delegate().remove(lockable);
	}

	@Override
	public boolean isPresent(final Lockable lockable) {
		return get(lockable).isPresent();
	}

	@Override
	public Optional<M> get(final Lockable lockable) {
		return fromNullable(delegate().get(lockable));
	}

	@Override
	public Iterable<Lockable> stored() {
		return newArrayList(delegate().keySet());
	}

	@Override
	public void removeAll() {
		delegate().clear();
	}

}
