package org.cmdbuild.common.api.mail;

public class MailException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static MailException content(final Throwable cause) {
		return new MailException(cause);
	}

	public static MailException fetch(final Throwable cause) {
		return new MailException(cause);
	}

	public static MailException get(final Throwable cause) {
		return new MailException(cause);
	}

	public static MailException input(final Throwable cause) {
		return new MailException(cause);
	}

	public static MailException io(final Throwable cause) {
		return new MailException(cause);
	}

	public static MailException move(final Throwable cause) {
		return new MailException(cause);
	}

	public static MailException creation(final Throwable cause) {
		return new MailException(cause);
	}

	public static MailException send(final Throwable cause) {
		return new MailException(cause);
	}

	private MailException(final Throwable cause) {
		super(cause);
	}

}
