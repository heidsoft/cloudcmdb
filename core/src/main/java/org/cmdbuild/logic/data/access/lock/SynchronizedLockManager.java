package org.cmdbuild.logic.data.access.lock;

public class SynchronizedLockManager extends ForwardingLockManager {

	private final LockManager delegate;

	public SynchronizedLockManager(final LockManager delegate) {
		this.delegate = delegate;
	}

	@Override
	protected LockManager delegate() {
		return delegate;
	}

	@Override
	public synchronized void lock(final Lockable lockable) throws LockedByAnother {
		super.lock(lockable);
	}

	@Override
	public synchronized void unlock(final Lockable lockable) throws LockedByAnother {
		super.unlock(lockable);
	}

	@Override
	public synchronized void unlockAll() {
		super.unlockAll();
	}

}
