package org.cmdbuild.auth;

import java.util.Collection;

import org.cmdbuild.auth.DefaultAuthenticationService.Configuration;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingConfiguration extends ForwardingObject implements Configuration {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingConfiguration() {
	}

	@Override
	protected abstract Configuration delegate();

	@Override
	public Collection<String> getActiveAuthenticators() {
		return delegate().getActiveAuthenticators();
	}

}
