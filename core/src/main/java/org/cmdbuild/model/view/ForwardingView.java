package org.cmdbuild.model.view;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingView extends ForwardingObject implements View {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingView() {
	}

	@Override
	protected abstract View delegate();

	@Override
	public Long getId() {
		return delegate().getId();
	}

	@Override
	public String getName() {
		return delegate().getName();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public String getSourceClassName() {
		return delegate().getSourceClassName();
	}

	@Override
	public String getSourceFunction() {
		return delegate().getSourceFunction();
	}

	@Override
	public String getFilter() {
		return delegate().getFilter();
	}

	@Override
	public ViewType getType() {
		return delegate().getType();
	}

	@Override
	public String getIdentifier() {
		return delegate().getIdentifier();
	}

	@Override
	public String getPrivilegeId() {
		return delegate().getPrivilegeId();
	}

}
