package org.cmdbuild.servlets.json.serializers.translations.table;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.LookupConverter;
import org.cmdbuild.servlets.json.serializers.translations.commons.LookupTypeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.LookupValueSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.LookupTypeEntry;
import org.cmdbuild.servlets.json.translationtable.objects.LookupValueEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class LookupTranslationSerializer implements TranslationSectionSerializer {

	private static final String LOOKUP_VALUE = "lookupValue";
	private static final String LOOKUP_TYPE = "lookupType";
	final TranslationLogic translationLogic;
	final boolean activeOnly;
	final LookupStore lookupStore;

	Ordering<LookupType> typeOrdering = LookupTypeSorter.DEFAULT.getOrientedOrdering();
	Ordering<Lookup> valueOrdering = LookupValueSorter.DEFAULT.getOrientedOrdering();

	LookupTranslationSerializer(final LookupStore lookupStore, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters) {
		this.lookupStore = lookupStore;
		this.activeOnly = activeOnly;
		this.translationLogic = translationLogic;
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		if (sorters != null) {
			try {
				for (int i = 0; i < sorters.length(); i++) {
					final JSONObject object = JSONObject.class.cast(sorters.get(i));
					final String element = object.getString(ELEMENT);
					if (element.equalsIgnoreCase(LOOKUP_TYPE)) {
						typeOrdering = LookupTypeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					} else if (element.equalsIgnoreCase(LOOKUP_VALUE)) {
						valueOrdering = LookupValueSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					}
				}
			} catch (final JSONException e) {
				// nothing to do
			}
		}
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<LookupType> allTypes = lookupStore.readAllTypes();

		final Iterable<? extends LookupType> sortedLookupTypes = typeOrdering.sortedCopy(allTypes);

		final Collection<TranslationSerialization> jsonLookupTypes = Lists.newArrayList();
		for (final LookupType type : sortedLookupTypes) {
			Iterable<Lookup> valuesOfType = lookupStore.readAll(type);
			if(activeOnly){
				valuesOfType = Iterables.filter(valuesOfType, new Predicate<Lookup>(){

					@Override
					public boolean apply(Lookup input) {
						return input.active();
					}
				});
			}

			final Iterable<Lookup> sortedLookupValues = valueOrdering.sortedCopy(valuesOfType);

			final Collection<LookupValueEntry> jsonValues = Lists.newArrayList();
			final LookupTypeEntry jsonType = new LookupTypeEntry();
			jsonType.setDescription(type.name);
			for (final Lookup value : sortedLookupValues) {
				final LookupValueEntry jsonValue = new LookupValueEntry();
				final String code = value.code();
				final String uuid = value.getTranslationUuid();
				jsonValue.setCode(code);
				jsonValue.setTranslationUuid(uuid);
				final Collection<EntryField> jsonFields = readFields(value);
				jsonValue.setFields(jsonFields);
				jsonValues.add(jsonValue);
			}
			jsonType.setValues(jsonValues);
			jsonLookupTypes.add(jsonType);
		}
		return jsonLookupTypes;
	}

	private Collection<EntryField> readFields(final Lookup value) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = LookupConverter.DESCRIPTION //
				.withIdentifier(value.getTranslationUuid()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final EntryField field = new EntryField();
		field.setName(LookupConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(value.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

}
