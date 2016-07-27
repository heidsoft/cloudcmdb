package org.cmdbuild.data.store.email;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingEmailTemplate extends ForwardingObject implements EmailTemplate {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEmailTemplate() {
	}

	@Override
	protected abstract EmailTemplate delegate();

	@Override
	public String getIdentifier() {
		return delegate().getIdentifier();
	}

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
	public String getFrom() {
		return delegate().getFrom();
	}

	@Override
	public String getTo() {
		return delegate().getTo();
	}

	@Override
	public String getCc() {
		return delegate().getCc();
	}

	@Override
	public String getBcc() {
		return delegate().getBcc();
	}

	@Override
	public String getSubject() {
		return delegate().getSubject();
	}

	@Override
	public String getBody() {
		return delegate().getBody();
	}

	@Override
	public Long getAccount() {
		return delegate().getAccount();
	}

	@Override
	public boolean isKeepSynchronization() {
		return delegate().isKeepSynchronization();
	}

	@Override
	public boolean isPromptSynchronization() {
		return delegate().isPromptSynchronization();
	}

	@Override
	public long getDelay() {
		return delegate().getDelay();
	}

}
