package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.serializers.translations.commons.ViewSorter;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class ViewSectionSerializer implements TranslationSectionSerializer {

	private final Iterable<String> selectedLanguages;
	private final TranslationLogic translationLogic;
	private final ViewLogic viewLogic;
	private final Ordering<View> viewOrdering = ViewSorter.DEFAULT.getOrientedOrdering();

	private final Collection<TranslationSerialization> records = Lists.newArrayList();

	public ViewSectionSerializer(final TranslationLogic translationLogic, final JSONArray sorters,
			final ViewLogic viewLogic, final Iterable<String> selectedLanguages) {
		this.viewLogic = viewLogic;
		this.translationLogic = translationLogic;
		this.selectedLanguages = selectedLanguages;
		// TODO: manage ordering configuration
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<View> views = viewLogic.fetchViewsOfAllTypes();
		final Iterable<View> sortedViews = viewOrdering.sortedCopy(views);

		for (final View view : sortedViews) {
			records.addAll(ViewSerializer.newInstance() //
					.withSelectedLanguages(selectedLanguages) //
					.withTranslationLogic(translationLogic) //
					.withViewLogic(viewLogic) //
					.withView(view) //
					.build() //
					.serialize());
		}
		return records;
	}

}
