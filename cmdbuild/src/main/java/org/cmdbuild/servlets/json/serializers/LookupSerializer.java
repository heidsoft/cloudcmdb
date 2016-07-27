package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.data.store.Storables.storableOf;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARENT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARENT_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATION_UUID;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.json.JSONException;
import org.json.JSONObject;

public class LookupSerializer {

	private final LookupStore lookupStore;
	private static final String MULTILEVEL_FORMAT = "%s - %s";

	public LookupSerializer(final LookupStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	public JSONObject serializeLookup(final Lookup lookup) throws JSONException {
		return serializeLookup(lookup, false);
	}

	public JSONObject serializeLookup(final Lookup lookup, final boolean shortForm) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put(ID_CAPITAL, lookup.getId());
			serializer.put(DESCRIPTION_CAPITAL, lookup.description());
			serializer.put("Number", lookup.number());

			if (!shortForm) {
				serializer.put("Type", lookup.type().name);
				serializer.put("Code", defaultIfBlank(lookup.code(), EMPTY));
				serializer.put("Notes", lookup.notes());
				serializer.put("Default", lookup.isDefault());
				serializer.put("Active", lookup.active());

				serializer.put("TranslationUuid", lookup.uuid());
			}

			final Lookup parent = lookup.parent();
			if (parent != null) {
				serializer.put("ParentId", parent.getId());
				if (!shortForm) {

					serializer.put(PARENT_DESCRIPTION, parent.description());
					serializer.put("ParentType", parent.type());
				}
			}
		}
		return serializer;
	}

	public JSONObject serializeLookupParent(final Lookup lookup) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put(PARENT_ID, lookup.getId());
			serializer.put(PARENT_DESCRIPTION, lookup.description());
		}
		return serializer;
	}

	public static JSONObject serializeLookupTable(final LookupType lookupType) throws JSONException {
		final JSONObject serializer = new JSONObject();
		serializer.put("id", lookupType.name);
		serializer.put("text", lookupType.name);
		serializer.put("type", "lookuptype");
		serializer.put("selectable", true);

		if (lookupType.parent != null) {
			serializer.put("parent", lookupType.parent);
		}
		return serializer;
	}

	public Map<String, Object> serializeLookupValue( //
			final LookupValue value //
	) {
		final Lookup lookup = lookup(value.getId());
		final Map<String, Object> out = new HashMap<String, Object>();
		out.put(ID, value.getId());
		out.put(DESCRIPTION, description(value));
		out.put(TRANSLATION_UUID, (lookup == null) ? null : lookup.uuid());
		return out;
	}

	private String description(final LookupValue value) {
		final Lookup _lookup = lookup(value.getId());
		final String lastLevelBaseDescription = (_lookup == null) ? value.getDescription() : _lookup.getDescription();
		String baseDescription = lastLevelBaseDescription;
		String jointBaseDescription = lastLevelBaseDescription;

		Lookup lookup = _lookup;
		if (lookup != null) {
			lookup = lookup(lookup.parentId());
			while (lookup != null) {
				final String parentBaseDescription = lookup.description();
				jointBaseDescription = String.format(MULTILEVEL_FORMAT, parentBaseDescription, baseDescription);
				lookup = lookup(lookup.parentId());
				baseDescription = jointBaseDescription;
			}
		}
		return jointBaseDescription;
	}

	private Lookup lookup(final Long id) {
		return (id == null) ? null : lookupStore.read(storableOf(id));
	}

}
