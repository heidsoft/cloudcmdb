package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.services.store.filter.FilterStore.Filter;
import org.cmdbuild.servlets.json.serializers.translations.commons.FilterSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class FilterSectionSerializer implements TranslationSectionSerializer {

	private final Iterable<String> selectedLanguages;
	private final TranslationLogic translationLogic;
	private final FilterLogic filterLogic;
	private final Ordering<Filter> filterOrdering = FilterSorter.DEFAULT.getOrientedOrdering();

	private final Collection<TranslationSerialization> records = Lists.newArrayList();

	public FilterSectionSerializer(final TranslationLogic translationLogic, final JSONArray sorters,
			final FilterLogic filterLogic, final Iterable<String> selectedLanguages) {
		this.filterLogic = filterLogic;
		this.translationLogic = translationLogic;
		this.selectedLanguages = selectedLanguages;
		// TODO: manage ordering configuration
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<org.cmdbuild.logic.filter.FilterLogic.Filter> allFilters = filterLogic.readShared(null, 0, 0);
		// TODO: implement ordering
		final Iterable<org.cmdbuild.logic.filter.FilterLogic.Filter> sortedFilters = allFilters;

		for (final org.cmdbuild.logic.filter.FilterLogic.Filter filter : sortedFilters) {
			records.addAll(FilterSerializer.newInstance() //
					.withSelectedLanguages(selectedLanguages) //
					.withTranslationLogic(translationLogic) //
					.withFilterLogic(filterLogic) //
					.withFilter(filter) //
					.build() //
					.serialize());
		}
		return records;
	}

}
