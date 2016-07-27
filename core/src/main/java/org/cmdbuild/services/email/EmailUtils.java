package org.cmdbuild.services.email;

import static java.util.regex.Pattern.MULTILINE;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.regex.Pattern;

public class EmailUtils {

	private static final String REGEX_TO_SEARCH = "(?<!>)$";
	private static final String REPLACEMENT = "<br>\n";
	private static final Pattern PATTERN = Pattern.compile(REGEX_TO_SEARCH, MULTILINE);

	public static String addLineBreakForHtml(final String value) {
		return PATTERN.matcher(defaultString(value)).replaceAll(REPLACEMENT);
	}

	private EmailUtils() {
		// prevents instantiation
	}

}
