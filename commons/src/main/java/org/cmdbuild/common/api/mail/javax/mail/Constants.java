package org.cmdbuild.common.api.mail.javax.mail;

class Constants {

	public static final String //
			CONTENT_TYPE_TEXT_HTML = "text/html", //
			CONTENT_TYPE_TEXT_PLAIN = "text/plain", //
			FALSE = "false", //
			IMAPS = "imaps", //
			MAIL_DEBUG = "mail.debug", //
			MAIL_IMAP_HOST = "mail.imap.host", //
			MAIL_IMAP_PORT = "mail.imap.port", //
			MAIL_IMAPS_HOST = "mail.imaps.host", //
			MAIL_IMAP_SOCKET_FACTORY_CLASS = "mail.imap.socketFactory.class", //
			MAIL_IMAPS_PORT = "mail.imaps.port", //
			MAIL_IMAP_STARTTLS_ENABLE = "mail.imap.starttls.enable", //
			MAIL_SMPT_SOCKET_FACTORY_CLASS = "mail.smpt.socketFactory.class", //
			MAIL_SMPT_SOCKET_FACTORY_FALLBACK = "mail.smtp.socketFactory.fallback", //
			MAIL_SMTP_AUTH = "mail.smtp.auth", //
			MAIL_SMTP_HOST = "mail.smtp.host", //
			MAIL_SMTP_PORT = "mail.smtp.port", //
			MAIL_SMTPS_AUTH = "mail.smtps.auth", //
			MAIL_SMTPS_HOST = "mail.smtps.host", //
			MAIL_SMTPS_PORT = "mail.smtps.port", //
			MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable", //
			MAIL_STORE_PROTOCOL = "mail.store.protocol", //
			MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol", //
			SMTPS = "smtps", //
			JAVAX_NET_SSL_SSL_SOCKET_FACTORY = "javax.net.ssl.SSLSocketFactory", //
			TRUE = "true";

	public static final PasswordAuthenticator NO_AUTENTICATION = null;

	public static class Header {

		public static final String MESSAGE_ID = "Message-ID";

		public static final String TO = "TO";
		public static final String CC = "CC";

		public static final String RECIPIENTS_SEPARATOR = ",";

		private Header() {
			// prevents instantiation
		}

	}

	private Constants() {
		// prevents instantiation
	}

}
