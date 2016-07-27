package org.cmdbuild.servlets.json.serializers.translations.table;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.translation.TranslationLogic;
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

public class ClassTranslationSerializer extends EntryTypeTranslationSerializer {

	private static final AttributesQuery NO_LIMIT_AND_OFFSET = new AttributesQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	ClassTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters) {
		super(dataLogic, activeOnly, translationLogic);
		setOrderings(sorters);
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<? extends CMClass> sortedClasses = sortedClasses();
		return serialize(sortedClasses);
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

	private Iterable<? extends CMClass> sortedClasses() {
		final Iterable<? extends CMClass> classes = dataLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> sortedClasses = entryTypeOrdering.sortedCopy(classes);
		return sortedClasses;
	}

	Iterable<TranslationSerialization> serialize(final Iterable<? extends CMClass> sortedClasses) {
		final Collection<TranslationSerialization> jsonClasses = Lists.newArrayList();
		for (final CMClass cmclass : sortedClasses) {
			final String className = cmclass.getName();
			final ParentEntry jsonClass = new ParentEntry();
			jsonClass.setName(className);
			final Collection<EntryField> classFields = readFields(cmclass);
			final Iterable<? extends CMAttribute> allAttributes = dataLogic.getAttributes(className, activeOnly,
					NO_LIMIT_AND_OFFSET);
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			final Collection<TableEntry> jsonAttributes = serializeAttributes(sortedAttributes);
			jsonClass.setChildren(jsonAttributes);
			jsonClass.setFields(classFields);
			jsonClasses.add(jsonClass);
		}
		return jsonClasses;
	}
}
