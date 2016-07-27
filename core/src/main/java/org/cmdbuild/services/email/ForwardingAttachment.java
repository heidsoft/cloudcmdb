package org.cmdbuild.services.email;

import javax.activation.DataHandler;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingAttachment extends ForwardingObject implements Attachment {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingAttachment() {
	}

	@Override
	protected abstract Attachment delegate();

	@Override
	public String getName() {
		return delegate().getName();
	}

	@Override
	public DataHandler getDataHandler() {
		return delegate().getDataHandler();
	}

}
