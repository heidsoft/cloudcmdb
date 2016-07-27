package org.cmdbuild.services;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingPatchManager extends ForwardingObject implements PatchManager {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingPatchManager() {
	}

	@Override
	protected abstract PatchManager delegate();

	@Override
	public void reset() {
		delegate().reset();
	}

	@Override
	public void applyPatchList() {
		delegate().applyPatchList();
	}

	@Override
	public Iterable<Patch> getAvaiblePatch() {
		return delegate().getAvaiblePatch();
	}

	@Override
	public boolean isUpdated() {
		return delegate().isUpdated();
	}

	@Override
	public void createLastPatch() {
		delegate().createLastPatch();
	}

}
