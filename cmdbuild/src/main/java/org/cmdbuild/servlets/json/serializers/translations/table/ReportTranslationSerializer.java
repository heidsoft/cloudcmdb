package org.cmdbuild.servlets.json.serializers.translations.table;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ReportConverter;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.serializers.translations.commons.ReportSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class ReportTranslationSerializer implements TranslationSectionSerializer {

	private final ReportStore reportStore;
	private final TranslationLogic translationLogic;
	Ordering<Report> ordering = ReportSorter.DEFAULT.getOrientedOrdering();

	public ReportTranslationSerializer(final ReportStore reportStore, final TranslationLogic translationLogic,
			final JSONArray sorters, final String separator, final SetupFacade setupFacade) {
		this.reportStore = reportStore;
		this.translationLogic = translationLogic;
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		// TODO
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Collection<Report> allReports = Lists.newArrayList();
		for (final ReportType type : ReportType.values()) {
			final Iterable<Report> reportsOfType = reportStore.findReportsByType(type);
			Iterables.addAll(allReports, reportsOfType);
		}
		final Iterable<Report> sorterReports = ordering.sortedCopy(allReports);
		return serialize(sorterReports);
	}

	private Iterable<TranslationSerialization> serialize(final Iterable<Report> sortedReports) {
		final Collection<TranslationSerialization> jsonReports = Lists.newArrayList();
		for (final Report report : sortedReports) {
			final String name = report.getCode();
			final TableEntry jsonReport = new TableEntry();
			jsonReport.setName(name);
			final Collection<EntryField> classFields = readFields(report);
			jsonReport.setFields(classFields);
			jsonReports.add(jsonReport);
		}
		return jsonReports;
	}

	private Collection<EntryField> readFields(final Report report) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = ReportConverter.DESCRIPTION //
				.withIdentifier(report.getCode()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final EntryField field = new EntryField();
		field.setName(ViewConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(report.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

}
