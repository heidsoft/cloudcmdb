package org.cmdbuild.common.api.mail;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingMailApi extends ForwardingObject implements MailApi {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingMailApi() {
	}

	@Override
	protected abstract MailApi delegate();

	@Override
	public SendableNewMail newMail() {
		return delegate().newMail();
	}

	@Override
	public NewMailQueue newMailQueue() {
		return delegate().newMailQueue();
	}

	@Override
	public SelectFolder selectFolder(final String folder) {
		return delegate().selectFolder(folder);
	}

	@Override
	public SelectMail selectMail(final FetchedMail mail) {
		return delegate().selectMail(mail);
	}

}
