package org.cmdbuild.dao.query.clause.alias;

import org.cmdbuild.dao.entrytype.CMEntryType;

public class Aliases {

	public static EntryTypeAlias canonical(final CMEntryType entryType) {
		return EntryTypeAlias.canonicalAlias(entryType);
	}

	public static NameAlias name(final String name) {
		return NameAlias.as(name);
	}
	
	/**
	 * Syntactic sugar.
	 * 
	 * @param alias
	 * 
	 * @return returns the same input parameter.
	 */
	public static Alias as(final Alias alias) {
		return alias;
	}

	private Aliases() {
		// prevents instantiation
	}

}
