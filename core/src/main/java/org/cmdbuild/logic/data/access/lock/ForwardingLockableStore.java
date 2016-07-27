package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.logic.data.access.lock.LockableStore.Lock;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;

public abstract class ForwardingLockableStore<L extends Lock> extends ForwardingObject implements LockableStore<L> {

	/**
	 * Usable by subclasses only
	 */
	protected ForwardingLockableStore() {
	}

	@Override
	protected abstract LockableStore<L> delegate();

	@Override
	public void add(final Lockable lockable, final L lock) {
		delegate().add(lockable, lock);
	}

	@Override
	public void remove(final Lockable lockable) {
		delegate().remove(lockable);
	}

	@Override
	public boolean isPresent(final Lockable lockable) {
		return delegate().isPresent(lockable);
	}

	@Override
	public Optional<L> get(final Lockable lockable) {
		return delegate().get(lockable);
	}

	@Override
	public Iterable<Lockable> stored() {
		return delegate().stored();
	}

	@Override
	public void removeAll() {
		delegate().removeAll();
	}

}
