package org.cmdbuild.logic.taskmanager.commons;

import org.cmdbuild.logic.Action;
import org.cmdbuild.scheduler.command.Command;

public class SchedulerCommandWrapper implements Command {

	public static SchedulerCommandWrapper of(final Action delegate) {
		return new SchedulerCommandWrapper(delegate);
	}

	private final Action delegate;

	private SchedulerCommandWrapper(final Action delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute() {
		delegate.execute();
	}

}