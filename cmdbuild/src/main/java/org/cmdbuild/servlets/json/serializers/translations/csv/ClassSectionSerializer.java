package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.cmdbuild.servlets.json.CommunicationConstants.NOTES;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.NO_LIMIT_AND_OFFSET;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.EntryTypeSorter;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ClassSectionSerializer extends EntryTypeTranslationSerializer {

	protected final Collection<TranslationSerialization> records = Lists.newArrayList();
	private final Predicate<CMAttribute> REMOVE_NOTES = new Predicate<CMAttribute>() {

		@Override
		public boolean apply(final CMAttribute input) {
			return !input.getName().equalsIgnoreCase(NOTES);
		}

	};

	public ClassSectionSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters, final String separator,
			final Iterable<String> selectedLanguages) {
		super(dataLogic, activeOnly, translationLogic, separator, selectedLanguages);
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		if (sorters != null) {
			try {
				for (int i = 0; i < sorters.length(); i++) {
					final JSONObject object = JSONObject.class.cast(sorters.get(i));
					final String element = object.getString(ELEMENT);
					if (element.equalsIgnoreCase(CLASS) || element.equalsIgnoreCase(PROCESS)) {
						entryTypeOrdering = EntryTypeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					} else if (element.equalsIgnoreCase(ATTRIBUTE)) {
						attributeOrdering = AttributeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					}
				}
			} catch (final JSONException e) {
				Log.JSONRPC.warn("ignoring malformed sorter");
			}
		}
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<? extends CMClass> sortedClasses = sortedClasses();
		serialize(sortedClasses);
		return records;
	}

	protected void serialize(final Iterable<? extends CMClass> classes) {
		for (final CMClass aClass : classes) {

			records.addAll(ClassSerializer.newInstance() //
					.withClass(aClass) //
					.withSelectedLanguages(selectedLanguages) //
					.withTranslationLogic(translationLogic) //
					.withDataAccessLogic(dataLogic) //
					.build() //
					.serialize());

			final Iterable<? extends CMAttribute> allAttributes = dataLogic.getAttributes(aClass.getName(), activeOnly,
					NO_LIMIT_AND_OFFSET);

			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(
					Iterables.filter(allAttributes, REMOVE_NOTES));
			for (final CMAttribute anAttribute : sortedAttributes) {

				records.addAll(AttributeSerializer.newInstance() //
						.withAttribute(anAttribute) //
						.withSelectedLanguages(selectedLanguages) //
						.withTranslationLogic(translationLogic) //
						.withDataAccessLogic(dataLogic) //
						.build() //
						.serialize());
			}
		}
	}

	private Iterable<? extends CMClass> sortedClasses() {
		final Iterable<? extends CMClass> classes = dataLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> sortedClasses = entryTypeOrdering.sortedCopy(classes);
		return sortedClasses;
	}

}
