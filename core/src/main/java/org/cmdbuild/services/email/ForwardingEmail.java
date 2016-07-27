package org.cmdbuild.services.email;

import org.joda.time.DateTime;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingEmail extends ForwardingObject implements Email {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEmail() {
	}

	@Override
	protected abstract Email delegate();

	@Override
	public DateTime getDate() {
		return delegate().getDate();
	}

	@Override
	public String getFromAddress() {
		return delegate().getFromAddress();
	}

	@Override
	public Iterable<String> getToAddresses() {
		return delegate().getToAddresses();
	}

	@Override
	public Iterable<String> getCcAddresses() {
		return delegate().getCcAddresses();
	}

	@Override
	public Iterable<String> getBccAddresses() {
		return delegate().getBccAddresses();
	}

	@Override
	public String getSubject() {
		return delegate().getSubject();
	}

	@Override
	public String getContent() {
		return delegate().getContent();
	}

	@Override
	public Iterable<Attachment> getAttachments() {
		return delegate().getAttachments();
	}

	@Override
	public String getAccount() {
		return delegate().getAccount();
	}

	@Override
	public long getDelay() {
		return delegate().getDelay();
	}

}
