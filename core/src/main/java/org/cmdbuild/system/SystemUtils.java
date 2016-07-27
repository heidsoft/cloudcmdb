package org.cmdbuild.system;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.defaultString;

public class SystemUtils {

	private static enum Property {

		MAIL_DEBUG_ENABLED("org.cmdbuild.mail.debug"), //
		;

		private final String key;

		private Property(final String key) {
			this.key = key;
		}

	}

	public static boolean isMailDebugEnabled() {
		final String value = defaultString(valueOf(Property.MAIL_DEBUG_ENABLED));
		return Boolean.parseBoolean(value);
	}

	private static String valueOf(final Property property) {
		return getProperty(property.key);
	}

	private SystemUtils() {
		// prevents instantiation
	}

	public static void main(final String[] args) {
		for (final Property value : Property.values()) {
			System.out.println(value.key);
		}
	}

}
