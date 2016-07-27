package org.cmdbuild.logic.taskmanager;

public abstract class AbstractScheduledTask implements ScheduledTask {

	/**
	 * Usable by subclasses only.
	 */
	protected AbstractScheduledTask() {
	}

	@Override
	public final boolean equals(final Object obj) {
		return doEquals(obj);
	}

	protected abstract boolean doEquals(Object obj);

	@Override
	public final int hashCode() {
		return doHashCode();
	}

	protected abstract int doHashCode();

}
