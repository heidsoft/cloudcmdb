package org.cmdbuild.logic.dms;

import org.cmdbuild.data.store.lookup.Lookup;

public class Utils {

	public static String valueForCategory(final Lookup input) {
		return (input == null) ? null : input.description();
	}

	private Utils() {
		// prevents instantiation
	}

}
