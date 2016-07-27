package org.cmdbuild.logic.taskmanager.task.connector;

import com.google.common.base.Predicate;

class NotificationEnabled implements Predicate<Void> {

	public static NotificationEnabled of(final ConnectorTask task) {
		return new NotificationEnabled(task);
	}

	private final ConnectorTask task;

	NotificationEnabled(final ConnectorTask task) {
		this.task = task;
	}

	@Override
	public boolean apply(final Void input) {
		return task.isNotificationActive();
	}

}