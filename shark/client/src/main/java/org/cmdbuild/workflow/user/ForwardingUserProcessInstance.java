package org.cmdbuild.workflow.user;

import java.util.List;

import org.cmdbuild.workflow.ForwardingProcessInstance;

public abstract class ForwardingUserProcessInstance extends ForwardingProcessInstance implements UserProcessInstance {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingUserProcessInstance() {
	}

	@Override
	protected abstract UserProcessInstance delegate();

	@Override
	public List<UserActivityInstance> getActivities() {
		return delegate().getActivities();
	}

	@Override
	public UserActivityInstance getActivityInstance(final String activityInstanceId) {
		return delegate().getActivityInstance(activityInstanceId);
	}

}
