package org.cmdbuild.common.api.mail;

import java.util.List;

import org.slf4j.Logger;

public class Configuration {

	/**
	 * Common configuration interface for {@link MailApi}.
	 */
	private static interface Common {

		/**
		 * Returns if the mail subsystem must be used in debug mode.
		 * 
		 * @return {@code true} if debug mode is active, {@code false}
		 *         otherwise.
		 */
		boolean isDebug();

		/**
		 * Returns the logger that can be used.
		 * 
		 * @return the {@link Logger}.
		 */
		Logger getLogger();

	}

	/**
	 * Output configuration interface for {@link MailApi}.
	 */
	public static interface Output extends Common {

		String PROTOCOL_SMTP = "smtp";
		String PROTOCOL_SMTPS = "smtps";

		/**
		 * Returns the protocol.
		 * 
		 * Can be {@code "smtp"} or {@code "smtps"}.
		 * 
		 * @return the protocol.
		 */
		String getOutputProtocol();

		/**
		 * Returns the status of StartTLS.
		 * 
		 * @return {@code true} if StartTLS is enabled, {@code false} otherwise.
		 */
		boolean isOutputStartTlsEnabled();

		/**
		 * Returns the host.
		 * 
		 * @return the host.
		 */
		String getOutputHost();

		/**
		 * Returns the port.
		 * 
		 * @return the port.
		 */
		Integer getOutputPort();

		/**
		 * Returns the username.
		 * 
		 * @return the username, can be {@code null}, empty or blank if
		 *         authentication is not required.
		 */
		String getOutputUsername();

		/**
		 * Returns the password.
		 * 
		 * @return the password.
		 */
		String getOutputPassword();

		/**
		 * Returns the addresses of the sender.
		 * 
		 * @return the addresses of the sender.
		 */
		List<String> getOutputFromRecipients();

		String getOutputFolder();

	}

	/**
	 * Input configuration interface for {@link MailApi}.
	 */
	public static interface Input extends Common {

		String PROTOCOL_IMAP = "imap";
		String PROTOCOL_IMAPS = "imaps";

		/**
		 * Returns the protocol.
		 * 
		 * Can be {@code "imap"} or {@code "imaps"}.
		 * 
		 * @return the protocol.
		 */
		String getInputProtocol();

		/**
		 * Returns the status of StartTLS.
		 * 
		 * @return {@code true} if StartTLS is enabled, {@code false} otherwise.
		 */
		boolean isInputStartTlsEnabled();

		/**
		 * Returns the host.
		 * 
		 * @return the host.
		 */
		String getInputHost();

		/**
		 * Returns the port.
		 * 
		 * @return the port.
		 */
		Integer getInputPort();

		/**
		 * Returns the username.
		 * 
		 * @return the username, can be {@code null}, empty or blank if
		 *         authentication is not required.
		 */
		String getInputUsername();

		/**
		 * Returns the password.
		 * 
		 * @return the password.
		 */
		String getInputPassword();

	}

	/**
	 * Configuration interface for {@link MailApi}.
	 */
	public static interface All extends Input, Output {

	}

	private Configuration() {
		// prevents instantiation
	}

}
