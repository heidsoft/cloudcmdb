package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.bouncycastle.util.Strings;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.EntryTypeSorter;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class DomainSectionSerializer extends EntryTypeTranslationSerializer {

	private final Collection<TranslationSerialization> records = Lists.newArrayList();

	public DomainSectionSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters, final String separator,
			final Iterable<String> languages) {
		super(dataLogic, activeOnly, translationLogic, separator, languages);
		setOrderings(sorters);
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {

		final Iterable<? extends CMDomain> allDomains = activeOnly ? dataLogic.findActiveDomains()
				: dataLogic.findAllDomains();
		final Iterable<? extends CMDomain> sortedDomains = entryTypeOrdering.sortedCopy(allDomains);

		for (final CMDomain aDomain : sortedDomains) {

			final Collection<? extends CsvTranslationRecord> allFieldsForDomain = DomainSerializer.newInstance() //
					.withDomain(aDomain) //
					.withSelectedLanguages(selectedLanguages) //
					.withTranslationLogic(translationLogic) //
					.withDataAccessLogic(dataLogic) //
					.build() //
					.serialize();

			final Collection<? extends CsvTranslationRecord> filteredDomainFields = Collections2
					.filter(allFieldsForDomain, new Predicate<CsvTranslationRecord>() {

						@Override
						public boolean apply(final CsvTranslationRecord input) {
							final String identifier = String.class.cast(input.getValues().get(IDENTIFIER));
							final String domainName = Strings.split(identifier, '.')[1];
							final String fieldName = Strings.split(identifier, '.')[2];
							final boolean isMasterDetail = dataLogic.findDomain(domainName).isMasterDetail();
							return !fieldName.equalsIgnoreCase(DomainConverter.masterDetail()) || isMasterDetail;
						}

					});

			records.addAll(filteredDomainFields);

			final Iterable<? extends CMAttribute> allAttributes = activeOnly ? aDomain.getActiveAttributes()
					: aDomain.getAllAttributes();

			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			for (final CMAttribute anAttribute : sortedAttributes) {
				records.addAll(DomainAttributeSerializer.newInstance() //
						.withAttribute(anAttribute) //
						.withSelectedLanguages(selectedLanguages) //
						.withTranslationLogic(translationLogic) //
						.withDataAccessLogic(dataLogic) //
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
					if (element.equalsIgnoreCase(DOMAIN)) {
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
				// nothing to do
			}
		}
	}
}
