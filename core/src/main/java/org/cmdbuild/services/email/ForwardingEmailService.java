package org.cmdbuild.services.email;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingEmailService extends ForwardingObject implements EmailService {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEmailService() {
	}

	@Override
	protected abstract EmailService delegate();

	@Override
	public void send(final Email email) throws EmailServiceException {
		delegate().send(email);
	}

	@Override
	public void receive(final Folders folders, final EmailCallbackHandler callback) throws EmailServiceException {
		delegate().receive(folders, callback);
	}

	@Override
	public Iterable<Email> receive(final Folders folders) throws EmailServiceException {
		return delegate().receive(folders);
	}

}
