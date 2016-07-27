package org.cmdbuild.common.template.engine;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import com.google.common.base.Joiner;

public class RegexUtils {

	public static String capture(final String value) {
		return String.format("(%s)", value);
	}

	public static String exclude(final String... values) {
		return exclude(Joiner.on(EMPTY).join(values));
	}

	public static String exclude(final String value) {
		return String.format("^%s", value);
	}

	public static String group(final String value) {
		return String.format("[%s]", value);
	}

	public static String oneOrMore(final String value) {
		return String.format("%s+", value);
	}

	public static String or(final String... values) {
		return Joiner.on("|").join(values);
	}

	public static String wordCharacter() {
		return "\\w";
	}

	public static String zeroOrOne(final String value) {
		return String.format("%s?", value);
	}

	private RegexUtils() {
		// prevents instantiation
	}

}
