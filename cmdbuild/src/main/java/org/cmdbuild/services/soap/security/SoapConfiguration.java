package org.cmdbuild.services.soap.security;

import java.util.Collection;

import org.cmdbuild.auth.DefaultAuthenticationService.Configuration;
import org.cmdbuild.auth.ForwardingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

public class SoapConfiguration extends ForwardingConfiguration {

	private final Configuration delegate;

	@Autowired
	public SoapConfiguration(final Configuration delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Configuration delegate() {
		return delegate;
	}

	@Override
	public Collection<String> getActiveAuthenticators() {
		return Sets.newHashSet(SoapPasswordAuthenticator.class.getSimpleName());
	}

}
