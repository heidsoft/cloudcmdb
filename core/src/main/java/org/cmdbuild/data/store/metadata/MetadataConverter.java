package org.cmdbuild.data.store.metadata;

import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Description;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Notes;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

import com.google.common.collect.Maps;

public class MetadataConverter extends BaseStorableConverter<Metadata> {

	public static MetadataConverter of(final Groupable groupable) {
		return new MetadataConverter(groupable);
	}

	public static final String CLASS_NAME = "Metadata";

	public static final String NAME = Description.getDBName();
	public static final String VALUE = Notes.getDBName();

	private final Groupable groupable;

	private MetadataConverter(final Groupable groupable) {
		this.groupable = groupable;
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return NAME;
	}

	@Override
	public Metadata convert(final CMCard card) {
		final String name = (String) card.getDescription();
		final String value = card.get(VALUE, String.class);
		return MetadataImpl.of(name, value);
	}

	@Override
	public Map<String, Object> getValues(final Metadata storable) {
		final Map<String, Object> values = Maps.newHashMap();
		if (groupable.getGroupAttributeName() != null) {
			values.put(groupable.getGroupAttributeName(), groupable.getGroupAttributeValue());
		}
		values.put(NAME, storable.name());
		values.put(VALUE, storable.value());
		return values;
	}

}
