package org.cmdbuild.servlets.json.serializers.translations.table;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.EntryTypeSorter;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.ParentEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class DomainTranslationSerializer extends EntryTypeTranslationSerializer {

	DomainTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters, final String separator,
			final SetupFacade setupFacade) {
		super(dataLogic, activeOnly, translationLogic);
		setOrderings(sorters);
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

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<? extends CMDomain> allDomains = activeOnly ? dataLogic.findActiveDomains()
				: dataLogic.findAllDomains();
		final Iterable<? extends CMDomain> sortedDomains = entryTypeOrdering.sortedCopy(allDomains);

		final Collection<TranslationSerialization> jsonDomains = Lists.newArrayList();
		for (final CMDomain domain : sortedDomains) {
			final String domainName = domain.getName();
			final Collection<EntryField> jsonFields = readFields(domain);
			final Iterable<? extends CMAttribute> allAttributes = domain.getAllAttributes();
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			final Collection<TableEntry> jsonAttributes = serializeAttributes(sortedAttributes);
			final ParentEntry jsonDomain = new ParentEntry();
			jsonDomain.setName(domainName);
			jsonDomain.setChildren(jsonAttributes);
			jsonDomain.setFields(jsonFields);
			jsonDomains.add(jsonDomain);
		}
		return jsonDomains;
	}

	private Collection<EntryField> readFields(final CMDomain domain) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final TranslationObject descriptionTranslationObject = DomainConverter.DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		final Map<String, String> descriptionTranslations = translationLogic //
				.readAll(descriptionTranslationObject);
		final EntryField descriptionField = new EntryField();
		descriptionField.setName(ClassConverter.description());
		descriptionField.setTranslations(descriptionTranslations);
		descriptionField.setValue(domain.getDescription());
		jsonFields.add(descriptionField);

		final TranslationObject directDescriptionTranslationObject = DomainConverter.DIRECT_DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		final Map<String, String> directDescriptionTranslations = translationLogic //
				.readAll(directDescriptionTranslationObject);
		final EntryField directDescriptionField = new EntryField();
		directDescriptionField.setName(DomainConverter.directDescription());
		directDescriptionField.setTranslations(directDescriptionTranslations);
		directDescriptionField.setValue(domain.getDescription1());
		jsonFields.add(directDescriptionField);

		final TranslationObject inverseDescriptionTranslationObject = DomainConverter.INVERSE_DESCRIPTION //
				.withIdentifier(domain.getName()) //
				.create();
		final Map<String, String> inverseDescriptionTranslations = translationLogic //
				.readAll(inverseDescriptionTranslationObject);
		final EntryField inverseDescriptionField = new EntryField();
		inverseDescriptionField.setName(DomainConverter.inverseDescription());
		inverseDescriptionField.setTranslations(inverseDescriptionTranslations);
		inverseDescriptionField.setValue(domain.getDescription2());
		jsonFields.add(inverseDescriptionField);

		if (isNotBlank(domain.getMasterDetailDescription())) {
			final TranslationObject masterDetailTranslationObject = DomainConverter.MASTERDETAIL_LABEL //
					.withIdentifier(domain.getName()) //
					.create();
			final Map<String, String> masterDetailTranslations = translationLogic //
					.readAll(masterDetailTranslationObject);
			final EntryField masterDetailLabelField = new EntryField();
			masterDetailLabelField.setName(DomainConverter.masterDetail());
			masterDetailLabelField.setTranslations(masterDetailTranslations);
			masterDetailLabelField.setValue(domain.getMasterDetailDescription());
			jsonFields.add(masterDetailLabelField);
		}

		return jsonFields;
	}

}
