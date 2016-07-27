package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.serializers.translations.commons.LookupTypeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.LookupValueSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class LookupSectionSerializer implements TranslationSectionSerializer {

	private static final String LOOKUP_VALUE = "lookupValue";
	private static final String LOOKUP_TYPE = "lookupType";
	final boolean activeOnly;
	final LookupStore lookupStore;

	Ordering<LookupType> typeOrdering = LookupTypeSorter.DEFAULT.getOrientedOrdering();
	Ordering<Lookup> valueOrdering = LookupValueSorter.DEFAULT.getOrientedOrdering();
	private final Iterable<String> selectedLanguages;
	private final TranslationLogic translationLogic;

	private final Collection<TranslationSerialization> records = Lists.newArrayList();

	public LookupSectionSerializer(final LookupStore lookupStore, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters,
			final Iterable<String> selectedLanguages) {
		this.selectedLanguages = selectedLanguages;
		this.translationLogic = translationLogic;
		this.lookupStore = lookupStore;
		this.activeOnly = activeOnly;
		setOrderings(sorters);
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<LookupType> allTypes = lookupStore.readAllTypes();

		final Iterable<? extends LookupType> sortedLookupTypes = typeOrdering.sortedCopy(allTypes);

		for (final LookupType type : sortedLookupTypes) {
			Iterable<Lookup> valuesOfType = lookupStore.readAll(type);

			if (activeOnly) {
				valuesOfType = Iterables.filter(valuesOfType, new Predicate<Lookup>() {

					@Override
					public boolean apply(final Lookup input) {
						return input.active();
					}
				});
			}

			final Iterable<Lookup> sortedLookupValues = valueOrdering.sortedCopy(valuesOfType);

			for (final Lookup value : sortedLookupValues) {
				records.addAll(LookupSerializer.newInstance() //
						.withSelectedLanguages(selectedLanguages) //
						.withTranslationLogic(translationLogic) //
						.withLookupType(type) //
						.withLookupValue(value) //
						.withLookupStore(lookupStore) //
						.build() //
						.serialize());
			}
		}
		return records;
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
}
