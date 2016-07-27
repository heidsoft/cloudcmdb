package org.cmdbuild.scheduler;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingJob extends ForwardingObject implements Job {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingJob() {
	}

	@Override
	protected abstract Job delegate();

	@Override
	public String getName() {
		return delegate().getName();
	}

	@Override
	public void execute() {
		delegate().execute();
	}

}
