package org.cmdbuild.auth.user;

import java.util.Collection;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingUser extends ForwardingObject implements CMUser {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingUser() {
	}

	@Override
	protected abstract CMUser delegate();

	@Override
	public Long getId() {
		return delegate().getId();
	}

	@Override
	public String getUsername() {
		return delegate().getUsername();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public Collection<String> getGroupNames() {
		return delegate().getGroupNames();
	}

	@Override
	public Collection<String> getGroupDescriptions() {
		return delegate().getGroupDescriptions();
	}

	@Override
	public String getDefaultGroupName() {
		return delegate().getDefaultGroupName();
	}

	@Override
	public String getEmail() {
		return delegate().getEmail();
	}

	@Override
	public boolean isActive() {
		return delegate().isActive();
	}

	@Override
	public boolean isService() {
		return delegate().isService();
	}

	@Override
	public boolean isPrivileged() {
		return delegate().isPrivileged();
	}

}
