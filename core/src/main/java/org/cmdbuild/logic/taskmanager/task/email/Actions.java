package org.cmdbuild.logic.taskmanager.task.email;

import static org.cmdbuild.logic.Logic.logger;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.services.email.Email;

public class Actions {

	private static class SafeAction implements Action {

		private final Action delegate;

		private SafeAction(final Action delegate) {
			this.delegate = delegate;
		}

		@Override
		public void execute(final Email email, final Storable storable) {
			try {
				delegate.execute(email, storable);
			} catch (final Throwable e) {
				logger.error("error executing action", e);
			}
		}

	}

	public static Action safe(final Action delegate) {
		return new SafeAction(delegate);
	}

	private Actions() {
		// prevents instantiation
	}

}
