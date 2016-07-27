package org.cmdbuild.scheduler.command;

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
	public void execute() {
		delegate().execute();
	}

}