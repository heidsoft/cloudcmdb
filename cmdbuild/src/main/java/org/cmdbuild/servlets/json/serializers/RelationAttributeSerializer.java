package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.data.store.Storables.storableOf;

import java.util.Map;

import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.json.JSONException;
import org.json.JSONObject;

public class RelationAttributeSerializer {

	public static interface Callback {

		void handle(final String name, final Object value);

	}

	private final LookupStore lookupStore;

	public RelationAttributeSerializer(final LookupStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	public final JSONObject toClient(final RelationInfo relationInfo) {
		return toClient(relationInfo, false);
	}

	public final JSONObject toClient(final RelationInfo relationInfo, final boolean cardReferencesWithIdAndDescription) {
		final JSONObject jsonAttributes = new JSONObject();
		toClient(relationInfo, new Callback() {

			@Override
			public void handle(final String name, final Object value) {
				try {
					jsonAttributes.put(name, value);
				} catch (final JSONException e) {
					throw new RuntimeException(e);
				}
			}

		}, cardReferencesWithIdAndDescription);
		return jsonAttributes;
	}

	public void toClient(final RelationInfo relationInfo, final Callback callback) {
		toClient(relationInfo, callback, false);
	}

	public void toClient(final RelationInfo relationInfo, final Callback callback,
			final boolean cardReferencesWithIdAndDescription) {
		final CMDomain domain = relationInfo.getRelation().getType();

		for (final Map.Entry<String, Object> attribute : relationInfo.getRelationAttributes()) {
			final CMAttributeType<?> attributeType = domain.getAttribute(attribute.getKey()).getType();
			final Object value = attribute.getValue();

			if (attributeType instanceof LookupAttributeType //
					&& value != null) { //

				final IdAndDescription cardReference = IdAndDescription.class.cast(value);
				Lookup lookup = null;
				if (cardReference.getId() != null) {
					lookup = lookupStore.read(storableOf(cardReference.getId()));
				}

				if (lookup != null) {
					attribute.setValue(new IdAndDescription(lookup.getId(), lookup.description()));
				}
			}

			final JavaToJSONValueConverter valueConverter = new JavaToJSONValueConverter(attributeType, //
					attribute.getValue(), //
					cardReferencesWithIdAndDescription //
			);

			callback.handle(attribute.getKey(), valueConverter.valueForJson());
		}
	}

}
