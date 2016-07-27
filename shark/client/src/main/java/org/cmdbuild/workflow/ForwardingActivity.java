package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.workflow.xpdl.CMActivityMetadata;
import org.cmdbuild.workflow.xpdl.CMActivityVariableToProcess;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingActivity extends ForwardingObject implements CMActivity {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingActivity() {
	}

	@Override
	protected abstract CMActivity delegate();

	@Override
	public String getId() {
		return delegate().getId();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public String getInstructions() {
		return delegate().getInstructions();
	}

	@Override
	public List<ActivityPerformer> getPerformers() {
		return delegate().getPerformers();
	}

	@Override
	public ActivityPerformer getFirstNonAdminPerformer() {
		return delegate().getFirstNonAdminPerformer();
	}

	@Override
	public List<CMActivityVariableToProcess> getVariables() {
		return delegate().getVariables();
	}

	@Override
	public Iterable<CMActivityMetadata> getMetadata() {
		return delegate().getMetadata();
	}

	@Override
	public List<CMActivityWidget> getWidgets() throws CMWorkflowException {
		return delegate().getWidgets();
	}

	@Override
	public List<CMActivityWidget> getWidgets(final CMValueSet processInstanceVariables) {
		return delegate().getWidgets(processInstanceVariables);
	}

}
