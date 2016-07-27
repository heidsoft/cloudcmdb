package org.cmdbuild.services.event;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingCommand extends ForwardingObject implements Command {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingCommand() {
	}

	@Override
	protected abstract Command delegate();

	@Override
	public void execute(Context context) {
		delegate().execute(context);
	}

}
