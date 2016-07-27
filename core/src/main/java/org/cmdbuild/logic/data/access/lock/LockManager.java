package org.cmdbuild.logic.data.access.lock;

import static java.lang.String.format;

import java.util.Date;

public interface LockManager {

	@SuppressWarnings("serial")
	class ExpectedLocked extends Exception {

		public ExpectedLocked() {
			super("should be locked");
		}

	}

	@SuppressWarnings("serial")
	class LockedByAnother extends Exception {

		private final String owner;
		private final Date time;

		public LockedByAnother(final String owner, final Date time) {
			super(format("locked by owner '%s' since '%s'", owner, time));
			this.owner = owner;
			this.time = time;
		}

		public String getOwner() {
			return owner;
		}

		public Date getTime() {
			return time;
		}

	}

	void lock(Lockable lockable) throws LockedByAnother;

	void unlock(Lockable lockable) throws LockedByAnother;

	void unlockAll();

	void checkNotLocked(Lockable lockable) throws LockedByAnother;

	void checkLockedByCurrent(Lockable lockable) throws LockedByAnother, ExpectedLocked;

}
