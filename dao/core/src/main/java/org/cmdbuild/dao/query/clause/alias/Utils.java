package org.cmdbuild.dao.query.clause.alias;

/**
 * @deprecated use {@link Aliases} instead.
 */
public class Utils {

	private Utils() {
		// prevents instantiation
	}

	/**
	 * @deprecated use {@link Aliases.as(Alias)} instead.
	 */
	public static Alias as(final Alias alias) {
		return Aliases.as(alias);
	}

}
