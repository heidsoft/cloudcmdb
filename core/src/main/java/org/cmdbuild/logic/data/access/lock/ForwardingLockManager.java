package org.cmdbuild.logic.data.access.lock;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingLockManager extends ForwardingObject implements LockManager {

	/**
	 * Usable by subclasses only.
	 */
	public ForwardingLockManager() {
	}

	@Override
	protected abstract LockManager delegate();

	@Override
	public void lock(final Lockable lockable) throws LockedByAnother {
		delegate().lock(lockable);
	}

	@Override
	public void unlock(final Lockable lockable) throws LockedByAnother {
		delegate().unlock(lockable);
	}

	@Override
	public void unlockAll() {
		delegate().unlockAll();
	}

	@Override
	public void checkNotLocked(final Lockable lockable) throws LockedByAnother {
		delegate().checkNotLocked(lockable);
	}

	@Override
	public void checkLockedByCurrent(final Lockable lockable) throws ExpectedLocked, LockedByAnother {
		delegate().checkLockedByCurrent(lockable);
	}

}
