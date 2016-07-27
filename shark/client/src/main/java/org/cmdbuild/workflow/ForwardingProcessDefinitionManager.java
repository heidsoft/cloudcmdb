package org.cmdbuild.workflow;

import javax.activation.DataSource;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingProcessDefinitionManager extends ForwardingObject implements ProcessDefinitionManager {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingProcessDefinitionManager() {
	}

	@Override
	protected abstract ProcessDefinitionManager delegate();

	@Override
	public DataSource getTemplate(final CMProcessClass process) throws CMWorkflowException {
		return delegate().getTemplate(process);
	}

	@Override
	public String[] getVersions(final CMProcessClass process) throws CMWorkflowException {
		return delegate().getVersions(process);
	}

	@Override
	public DataSource getDefinition(final CMProcessClass process, final String version) throws CMWorkflowException {
		return delegate().getDefinition(process, version);
	}

	@Override
	public void updateDefinition(final CMProcessClass process, final DataSource pkgDefData) throws CMWorkflowException {
		delegate().updateDefinition(process, pkgDefData);
	}

	@Override
	public CMActivity getManualStartActivity(final CMProcessClass process, final String groupName)
			throws CMWorkflowException {
		return delegate().getManualStartActivity(process, groupName);
	}

	@Override
	public CMActivity getActivity(final CMProcessInstance processInstance, final String activityDefinitionId)
			throws CMWorkflowException {
		return delegate().getActivity(processInstance, activityDefinitionId);
	}

	@Override
	public String getPackageId(final CMProcessClass process) throws CMWorkflowException {
		return delegate().getPackageId(process);
	}

	@Override
	public String getProcessDefinitionId(final CMProcessClass process) throws CMWorkflowException {
		return delegate().getProcessDefinitionId(process);
	}

	@Override
	public String getProcessClassName(final String processDefinitionId) throws CMWorkflowException {
		return delegate().getProcessClassName(processDefinitionId);
	}

}
