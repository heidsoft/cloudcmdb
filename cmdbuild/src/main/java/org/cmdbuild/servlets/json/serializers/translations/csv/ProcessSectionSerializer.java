package org.cmdbuild.servlets.json.serializers.translations.csv;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.base.Predicate;

public class ProcessSectionSerializer extends ClassSectionSerializer {

	public ProcessSectionSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters, final String separator,
			final Iterable<String> selectedLanguages) {
		super(dataLogic, activeOnly, translationLogic, sorters, separator, selectedLanguages);
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<? extends CMClass> sortedProcesses = sortedProcesses();
		serialize(sortedProcesses);
		return records;
	}

	private Iterable<? extends CMClass> sortedProcesses() {

		final Iterable<? extends CMClass> allClasses = dataLogic.findAllClasses();
		final Iterable<? extends CMClass> onlyProcessess = from(allClasses).filter(new Predicate<CMClass>() {

			@Override
			public boolean apply(final CMClass input) {
				final CMClass processBaseClass = dataLogic.findClass(Constants.BASE_PROCESS_CLASS_NAME);
				return processBaseClass.isAncestorOf(input);
			}
		});
		if (activeOnly) {
			from(onlyProcessess).filter(new Predicate<CMClass>() {
				@Override
				public boolean apply(final CMClass input) {
					return input.isActive();
				}
			});
		}
		return entryTypeOrdering.sortedCopy(onlyProcessess);
	}

}
