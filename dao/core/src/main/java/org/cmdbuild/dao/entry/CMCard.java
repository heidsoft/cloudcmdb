package org.cmdbuild.dao.entry;

import java.util.Map.Entry;

import org.cmdbuild.dao.entrytype.CMClass;

/**
 * Immutable card.
 */
public interface CMCard extends CMEntry {

	/**
	 * {@link CMCard} mutator object.
	 */
	interface CMCardDefinition extends CMEntryDefinition {

		@Override
		CMCardDefinition set(String key, Object value);

		@Override
		CMCardDefinition set(Iterable<? extends Entry<String, ? extends Object>> keysAndValues);

		@Override
		CMCardDefinition setUser(String user);

		CMCardDefinition setCode(Object value);

		CMCardDefinition setDescription(Object value);

		CMCardDefinition setCurrentId(Long currentId);

		@Override
		CMCard save();

	}

	@Override
	CMClass getType();

	/**
	 * Returns the value of the "Code" attribute.
	 * 
	 * @return the value of the attribute.
	 * 
	 * @throws IllegalArgumentException
	 *             if attribute is not present.
	 */
	Object getCode();

	/**
	 * Returns the value of the "Description" attribute.
	 * 
	 * @return the value of the attribute.
	 * 
	 * @throws IllegalArgumentException
	 *             if attribute is not present.
	 */
	Object getDescription();

	Long getCurrentId();

}
