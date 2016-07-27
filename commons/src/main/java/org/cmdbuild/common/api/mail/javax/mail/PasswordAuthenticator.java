package org.cmdbuild.common.api.mail.javax.mail;

import javax.mail.PasswordAuthentication;

import org.cmdbuild.common.api.mail.Configuration;

class PasswordAuthenticator extends javax.mail.Authenticator {

	private final PasswordAuthentication authentication;

	public PasswordAuthenticator(final String username, final String password) {
		authentication = new PasswordAuthentication(username, password);
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return authentication;
	}

	public static PasswordAuthenticator from(final Configuration.Input configuration) {
		return new PasswordAuthenticator(configuration.getInputUsername(), configuration.getInputPassword());
	}

	public static PasswordAuthenticator from(final Configuration.Output configuration) {
		return new PasswordAuthenticator(configuration.getOutputUsername(), configuration.getOutputPassword());
	}

}
