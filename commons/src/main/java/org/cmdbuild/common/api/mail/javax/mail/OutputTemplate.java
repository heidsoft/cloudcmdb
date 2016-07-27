package org.cmdbuild.common.api.mail.javax.mail;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.FALSE;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.JAVAX_NET_SSL_SSL_SOCKET_FACTORY;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_DEBUG;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMPT_SOCKET_FACTORY_CLASS;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMPT_SOCKET_FACTORY_FALLBACK;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMTPS_AUTH;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMTPS_HOST;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMTPS_PORT;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMTP_AUTH;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMTP_HOST;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMTP_PORT;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_SMTP_STARTTLS_ENABLE;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.MAIL_TRANSPORT_PROTOCOL;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.NO_AUTENTICATION;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.SMTPS;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.TRUE;

import java.io.PrintStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.commons.io.output.WriterOutputStream;
import org.cmdbuild.common.api.mail.Configuration;
import org.cmdbuild.common.api.mail.MailException;
import org.slf4j.Logger;

class OutputTemplate {

	public static interface Hook {

		void connected(Session session, Transport transport) throws MessagingException;

	}

	private final Configuration.Output configuration;
	private final Logger logger;
	private final PrintStream debugOutput;

	public OutputTemplate(final Configuration.Output configuration) {
		this.configuration = configuration;
		this.logger = configuration.getLogger();
		this.debugOutput = new PrintStream(new WriterOutputStream(new LoggerWriter(logger)), true);
	}

	public void execute(final Hook hook) {
		Transport transport = null;
		try {
			final Session session = createSession();
			transport = session.getTransport();
			transport.connect();
			hook.connected(session, transport);
		} catch (final MessagingException e) {
			logger.error("error while connecting/connected to store", e);
			throw MailException.send(e);
		} catch (final Exception e) {
			logger.error("error while connecting/connected to store", e);
			throw MailException.send(e);
		} finally {
			if (transport != null && transport.isConnected()) {
				try {
					transport.close();
				} catch (final MessagingException e) {
					logger.warn("error closing transport, ignoring it", e);
				}
			}
		}
	}

	private Session createSession() {
		final Properties properties = createConfigurationProperties();
		final Authenticator authenticator = getAutenticator();
		final Session session = Session.getInstance(properties, authenticator);
		session.setDebugOut(debugOutput);
		return session;
	}

	private Properties createConfigurationProperties() {
		final Properties properties = new Properties(System.getProperties());
		properties.setProperty(MAIL_DEBUG, Boolean.toString(configuration.isDebug()));
		properties.setProperty(MAIL_TRANSPORT_PROTOCOL, configuration.getOutputProtocol());
		properties.setProperty(MAIL_SMTP_STARTTLS_ENABLE, configuration.isOutputStartTlsEnabled() ? TRUE : FALSE);
		final String auth = authenticationRequired() ? TRUE : FALSE;
		if (sslRequired()) {
			properties.setProperty(MAIL_SMTPS_HOST, configuration.getOutputHost());
			if (configuration.getOutputPort() != null) {
				properties.setProperty(MAIL_SMTPS_PORT, configuration.getOutputPort().toString());
			}
			properties.setProperty(MAIL_SMTPS_AUTH, auth);
			properties.setProperty(MAIL_SMPT_SOCKET_FACTORY_CLASS, JAVAX_NET_SSL_SSL_SOCKET_FACTORY);
			properties.setProperty(MAIL_SMPT_SOCKET_FACTORY_FALLBACK, FALSE);
		} else {
			properties.setProperty(MAIL_SMTP_HOST, configuration.getOutputHost());
			if (configuration.getOutputPort() != null) {
				properties.setProperty(MAIL_SMTP_PORT, configuration.getOutputPort().toString());
			}
			properties.setProperty(MAIL_SMTP_AUTH, auth);
		}
		logger.trace("properties: {}", properties);
		return properties;
	}

	private boolean authenticationRequired() {
		return isNotBlank(configuration.getOutputUsername());
	}

	private boolean sslRequired() {
		return SMTPS.equals(configuration.getOutputProtocol());
	}

	private Authenticator getAutenticator() {
		return authenticationRequired() ? PasswordAuthenticator.from(configuration) : NO_AUTENTICATION;
	}

}
