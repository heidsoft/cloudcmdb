package org.cmdbuild.listeners;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.notification.Notifier;

public class ContextStoreNotifier implements Notifier {

	private final ContextStore contextStore;

	public ContextStoreNotifier(final ContextStore contextStore) {
		this.contextStore = contextStore;
	}

	@Override
	public void warn(final CMDBException e) {
		contextStore.get().get().pushWarning(e);
	}

}
