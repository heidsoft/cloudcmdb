package org.cmdbuild.logic.icon;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingIcon extends ForwardingObject implements Icon {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingIcon() {
	}

	@Override
	protected abstract Icon delegate();

	@Override
	public Long getId() {
		return delegate().getId();
	}

	@Override
	public Type getType() {
		return delegate().getType();
	}

	@Override
	public Image getImage() {
		return delegate().getImage();
	}

}
