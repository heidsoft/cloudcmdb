package org.cmdbuild.servlets.json.serializers.translations.table;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.base.Predicate;

public class ProcessTranslationSerializer extends ClassTranslationSerializer {

	ProcessTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters, final String separator,
			final SetupFacade setupFacade) {
		super(dataLogic, activeOnly, translationLogic, sorters);
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
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
		final Iterable<? extends CMClass> sortedProcesses = entryTypeOrdering.sortedCopy(onlyProcessess);
		return serialize(sortedProcesses);
	}

}
