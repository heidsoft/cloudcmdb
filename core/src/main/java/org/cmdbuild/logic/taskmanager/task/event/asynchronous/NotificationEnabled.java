package org.cmdbuild.logic.taskmanager.task.event.asynchronous;

import com.google.common.base.Predicate;

class NotificationEnabled implements Predicate<Void> {

	public static NotificationEnabled of(final AsynchronousEventTask task) {
		return new NotificationEnabled(task);
	}

	private final AsynchronousEventTask task;

	NotificationEnabled(final AsynchronousEventTask task) {
		this.task = task;
	}

	@Override
	public boolean apply(final Void input) {
		return task.isNotificationActive();
	}

}