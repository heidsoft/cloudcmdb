package util;

import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

public class Utils {

	private Utils() {
		// prevents instantiation
	}

	public static String clean(final String sql) {
		return sql //
				.replace(LINE_SEPARATOR, " ") //
				.replaceAll("[ ]+", " ") //
		;
	}

}
