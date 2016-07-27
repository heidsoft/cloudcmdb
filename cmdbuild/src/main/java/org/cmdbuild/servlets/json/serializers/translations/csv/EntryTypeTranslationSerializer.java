package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.nullableIterable;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.EntryTypeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public abstract class EntryTypeTranslationSerializer implements TranslationSectionSerializer {

	final DataAccessLogic dataLogic;
	Iterable<String> selectedLanguages;
	final TranslationLogic translationLogic;
	final boolean activeOnly;
	final String separator;
	final String IDENTIFIER = "identifier";
	final String DESCRIPTION = "description";
	final List<String> commonHeaders = Lists.newArrayList(IDENTIFIER, DESCRIPTION);
	String[] csvHeader;

	Ordering<CMEntryType> entryTypeOrdering = EntryTypeSorter.DEFAULT.getOrientedOrdering();
	Ordering<CMAttribute> attributeOrdering = AttributeSorter.DEFAULT.getOrientedOrdering();

	EntryTypeTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final String separator, final Iterable<String> selectedLanguages) {
		this.dataLogic = dataLogic;
		this.activeOnly = activeOnly;
		this.translationLogic = translationLogic;
		this.separator = separator;
		this.selectedLanguages = selectedLanguages;
	}

	@Override
	public abstract Iterable<TranslationSerialization> serialize();

	Iterable<? extends CMAttribute> sortAttributes(final Iterable<? extends CMAttribute> allAttributes) {
		final Iterable<? extends CMAttribute> sortedAttributes = attributeOrdering
				.sortedCopy(nullableIterable(allAttributes));
		return sortedAttributes;
	}

}
