package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.CODE_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.LOOKUP_LIST;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.ORIG_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARENT_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.SHORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE_CAPITAL;

import java.util.Map;
import java.util.UUID;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupImpl;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupQuery;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupTypeQuery;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.LookupSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

public class ModLookup extends JSONBaseWithSpringContext {

	private static final LookupTypeQuery UNUSED_LOOKUP_TYPE_QUERY = new LookupTypeQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	private static final LookupQuery UNUSED_LOOKUP_QUERY = new LookupQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	@JSONExported
	public JSONArray tree() throws JSONException {
		final Iterable<LookupType> elements = lookupLogic().getAllTypes(UNUSED_LOOKUP_TYPE_QUERY);

		final JSONArray jsonLookupTypes = new JSONArray();
		for (final LookupType element : elements) {
			jsonLookupTypes.put(LookupSerializer.serializeLookupTable(element));
		}

		return jsonLookupTypes;
	}

	@JSONExported
	@Admin
	public JSONObject saveLookupType( //
			final JSONObject serializer, //
			final @Parameter(DESCRIPTION) String type, //
			final @Parameter(ORIG_TYPE) String originalType, //
			final @Parameter(value = PARENT, required = false) String parentType //
	) throws JSONException {
		final LookupType newType = LookupType.newInstance() //
				.withName(type)//
				.withParent(parentType)//
				.build();
		final LookupType oldType = LookupType.newInstance() //
				.withName(originalType)//
				.withParent(parentType)//
				.build();
		lookupLogic().saveLookupType(newType, oldType);

		final JSONObject jsonLookupType = LookupSerializer.serializeLookupTable(newType);
		serializer.put("lookup", jsonLookupType);
		if (isNotEmpty(originalType)) {
			jsonLookupType.put("oldId", originalType);
		} else {
			serializer.put("isNew", true);
		}

		return serializer;
	}

	@JSONExported
	public JSONObject getLookupList( //
			final JSONObject serializer, //
			final @Parameter(TYPE) String type, //
			final @Parameter(ACTIVE) boolean active, //
			final @Parameter(value = SHORT, required = false) boolean shortForm) //
			throws JSONException {

		final LookupType lookupType = LookupType.newInstance().withName(type).build();
		final Iterable<Lookup> elements = lookupLogic().getAllLookup(lookupType, active, UNUSED_LOOKUP_QUERY);

		final LookupSerializer lookupSerializer = lookupSerializer();

		for (final Lookup element : elements) {
			serializer.append("rows", lookupSerializer.serializeLookup(element, shortForm));
		}

		serializer.put("total", size(elements));
		return serializer;
	}

	@JSONExported
	public JSONObject getParentList( //
			final @Parameter(value = TYPE, required = false) String type //
	) throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final LookupType lookupType = LookupType.newInstance().withName(type).build();
		final Iterable<Lookup> elements = lookupLogic().getAllLookupOfParent(lookupType);

		final LookupSerializer lookupSerializer = lookupSerializer();

		for (final Lookup lookup : elements) {
			out.append("rows", lookupSerializer.serializeLookupParent(lookup));
		}

		return out;
	}

	@JSONExported
	@Admin
	public void disableLookup( //
			@Parameter(ID) final int id //
	) {
		lookupLogic().disableLookup(Long.valueOf(id));
	}

	@JSONExported
	@Admin
	public void enableLookup( //
			@Parameter(ID) final int id //
	) {
		lookupLogic().enableLookup(Long.valueOf(id));
	}

	@JSONExported
	@Admin
	public JSONObject saveLookup( //
			final JSONObject serializer, //
			final @Parameter(TYPE_CAPITAL) String type, //
			final @Parameter(CODE_CAPITAL) String code, //
			final @Parameter(DESCRIPTION_CAPITAL) String description, //
			final @Parameter(ID_CAPITAL) int id, //
			final @Parameter(PARENT_ID) int parentId, //
			final @Parameter(NOTES) String notes, //
			final @Parameter(DEFAULT) boolean isDefault, //
			final @Parameter(ACTIVE_CAPITAL) boolean isActive //
	) throws JSONException {

		final String translationUuid = defaultIfBlank(lookupLogic().fetchTranslationUuid(id), UUID.randomUUID()
				.toString());

		final LookupImpl lookup = LookupImpl.newInstance() //
				.withId(Long.valueOf(id)) //
				.withCode(code) //
				.withDescription(description) //
				.withType(LookupType.newInstance() //
						.withName(type)) //
				.withParentId(Long.valueOf(parentId)) //
				.withNotes(notes) //
				.withDefaultStatus(isDefault) //
				.withActiveStatus(isActive) //
				.withUuid(translationUuid) //
				.build();

		final Long lookupId = lookupLogic().createOrUpdateLookup(lookup);
		lookup.setId(lookupId);
		final LookupSerializer lookupSerializer = lookupSerializer();
		serializer.put("lookup", lookupSerializer.serializeLookup(lookup));
		return serializer;
	}

	@JSONExported
	@Admin
	public void reorderLookup( //
			final @Parameter(TYPE) String type, //
			final @Parameter(LOOKUP_LIST) JSONArray jsonPositions //
	) throws JSONException, AuthException {
		final LookupType lookupType = LookupType.newInstance() //
				.withName(type) //
				.build();
		final Map<Long, Integer> positions = Maps.newHashMap();
		for (int i = 0; i < jsonPositions.length(); i++) {
			final JSONObject jsonElement = jsonPositions.getJSONObject(i);
			positions.put( //
					Long.valueOf(jsonElement.getInt("id")), //
					jsonElement.getInt("index"));
		}
		lookupLogic().reorderLookup(lookupType, positions);
	}

}
