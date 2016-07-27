package org.cmdbuild.dao.entry;

import java.util.Map.Entry;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;

public class DBCard extends DBEntry implements CMCard, CMCardDefinition {

	private static final Long NOT_EXISTING_CARD_ID = null;
	private Long currentId;

	private DBCard(final DBDriver driver, final CMClass type, final Long id) {
		super(driver, type, id);
	}

	@Override
	public final DBCard set(final String key, final Object value) {
		setOnly(key, value);
		return this;
	}

	@Override
	public final DBCard set(final Iterable<? extends Entry<String, ? extends Object>> keysAndValues) {
		for (final Entry<String, ? extends Object> entry : keysAndValues) {
			set(entry.getKey(), entry.getValue());
		}
		return this;
	}

	@Override
	public DBCard setUser(final String user) {
		super.setUser(user);
		return this;
	}

	@Override
	public CMClass getType() {
		return (CMClass) super.getType();
	}

	@Override
	public Object getCode() {
		return get(getType().getCodeAttributeName());
	}

	@Override
	public CMCardDefinition setCode(final Object value) {
		return set(getType().getCodeAttributeName(), value);
	}

	@Override
	public Object getDescription() {
		return get(getType().getDescriptionAttributeName());
	}

	@Override
	public CMCardDefinition setDescription(final Object value) {
		return set(getType().getDescriptionAttributeName(), value);
	}

	@Override
	public CMCardDefinition setCurrentId(final Long currentId) {
		this.currentId = currentId;
		return this;
	}

	@Override
	public Long getCurrentId() {
		return currentId;
	}

	@Override
	public DBCard save() {
		if (getId() == NOT_EXISTING_CARD_ID) {
			saveOnly();
		} else {
			updateOnly();
		}
		return this;
	}

	public static DBCard newInstance(final DBDriver driver, final CMClass type) {
		return new DBCard(driver, type, NOT_EXISTING_CARD_ID);
	}

	public static DBCard newInstance(final DBDriver driver, final CMClass type, final Long id) {
		return new DBCard(driver, type, id);
	}

}
