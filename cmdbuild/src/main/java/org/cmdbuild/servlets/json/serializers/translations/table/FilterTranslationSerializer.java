package org.cmdbuild.servlets.json.serializers.translations.table;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.FilterConverter;
import org.cmdbuild.services.store.filter.FilterStore.Filter;
import org.cmdbuild.servlets.json.serializers.translations.commons.FilterSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class FilterTranslationSerializer implements TranslationSectionSerializer {

	private final FilterLogic filterLogic;
	private final TranslationLogic translationLogic;
	Ordering<Filter> filterOrdering = FilterSorter.DEFAULT.getOrientedOrdering();

	public FilterTranslationSerializer(final FilterLogic filterLogic, final TranslationLogic translationLogic,
			final JSONArray sorters, final String separator, final SetupFacade setupFacade) {
		this.filterLogic = filterLogic;
		this.translationLogic = translationLogic;
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<org.cmdbuild.logic.filter.FilterLogic.Filter> allFilters = filterLogic.readShared(null, 0, 0);
		// TODO: implement ordering
		final Iterable<org.cmdbuild.logic.filter.FilterLogic.Filter> sortedFilters = allFilters;
		final Collection<TranslationSerialization> jsonFilters = Lists.newArrayList();
		for (final org.cmdbuild.logic.filter.FilterLogic.Filter filter : sortedFilters) {
			final String name = filter.getName();
			final TableEntry jsonFilter = new TableEntry();
			jsonFilter.setName(name);
			final Collection<EntryField> fields = readFields(filter);
			jsonFilter.setFields(fields);
			jsonFilters.add(jsonFilter);
		}
		return jsonFilters;
	}

	private Collection<EntryField> readFields(final org.cmdbuild.logic.filter.FilterLogic.Filter filter) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = FilterConverter.DESCRIPTION //
				.withIdentifier(filter.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final EntryField field = new EntryField();
		field.setName(FilterConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(filter.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

}
