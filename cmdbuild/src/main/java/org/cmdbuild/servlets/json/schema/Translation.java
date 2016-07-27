package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE_ONLY;
import static org.cmdbuild.servlets.json.CommunicationConstants.FIELD;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILE;
import static org.cmdbuild.servlets.json.CommunicationConstants.LANGUAGES;
import static org.cmdbuild.servlets.json.CommunicationConstants.SEPARATOR;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATIONS;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.IDENTIFIER;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.OWNER;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.TYPE;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.commonHeaders;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.createConverter;
import static org.cmdbuild.servlets.json.serializers.translations.csv.read.ErrorNotifier.THROWS_EXCEPTION;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.Validate;
import org.bouncycastle.util.Strings;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.DefaultRecordDeserializer;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.ErrorListener;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.LoggingErrorNotifier;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.RecordDeserializer;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.SafeRecordDeserializer;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory.Output;
import org.cmdbuild.servlets.json.serializers.translations.table.TranslationSerializerFactory.Sections;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;
import org.cmdbuild.servlets.json.translationtable.objects.csv.DefaultCsvExporter;
import org.cmdbuild.servlets.json.translationtable.objects.csv.DefaultCsvImporter;
import org.cmdbuild.servlets.utils.FileItemDataSource;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Translation extends JSONBaseWithSpringContext {

	private static final String ALL = "ALL";

	private static final String FAILURES = "failures";

	private static final Function<Iterable<TranslationSerialization>, Iterable<Map<String, Object>>> TO_MAP = //
	new Function<Iterable<TranslationSerialization>, Iterable<Map<String, Object>>>() {

		@Override
		public Iterable<Map<String, Object>> apply(final Iterable<TranslationSerialization> input) {
			final Collection<Map<String, Object>> records = Lists.newArrayList();

			for (final TranslationSerialization serialization : input) {
				final CsvTranslationRecord record = CsvTranslationRecord.class.cast(serialization);
				records.add(record.getValues());
			}
			return records;
		}
	};

	private static final Function<Map<TranslationSerialization, Throwable>, Iterable<Map<String, String>>> TO_CLIENT = //
	new Function<Map<TranslationSerialization, Throwable>, Iterable<Map<String, String>>>() {
		@Override
		public Iterable<Map<String, String>> apply(final Map<TranslationSerialization, Throwable> input) {
			final Collection<Map<String, String>> output = Lists.newArrayList();
			for (final Entry<TranslationSerialization, Throwable> entry : input.entrySet()) {
				final Map<String, String> inner = Maps.newHashMap();
				inner.put("record", entry.getKey().toString());
				inner.put("message", entry.getValue().getMessage());
				output.add(inner);
			}
			return output;
		}
	};

	@JSONExported
	@Admin
	public JsonResponse read( //
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = OWNER, required = false) final String owner, //
			@Parameter(value = IDENTIFIER) final String identifier, //
			@Parameter(value = FIELD) final String field //
	) {
		Validate.notBlank(type);
		Validate.notBlank(identifier);
		Validate.notBlank(field);

		final Converter converter = createConverter(type, field);
		final TranslationObject translationObject = converter.withOwner(owner) //
				.withIdentifier(identifier) //
				.create();
		final Map<String, String> translations = translationLogic().readAll(translationObject);
		return JsonResponse.success(translations);
	}

	@JSONExported
	@Admin
	public void update( //
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = OWNER, required = false) final String owner, //
			@Parameter(value = IDENTIFIER) final String identifier, //
			@Parameter(value = FIELD) final String field, //
			@Parameter(value = TRANSLATIONS) final JSONObject translations //
	) {
		Validate.notBlank(type);
		Validate.notBlank(identifier);
		Validate.notBlank(field);

		final Converter converter = createConverter(type, field);

		final TranslationObject translationObject = converter //
				.withOwner(owner) //
				.withIdentifier(identifier) //
				.withTranslations(toMap(translations)) //
				.create();
		translationLogic().update(translationObject);
	}

	@Admin
	@JSONExported(contentType = "text/csv")
	public DataHandler exportCsv(@Parameter(value = TYPE) final String type, //
			@Parameter(value = SEPARATOR, required = false) final String separator, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = ACTIVE_ONLY, required = false) final boolean activeOnly, //
			@Parameter(value = LANGUAGES, required = false) final String _languages //
	) throws JSONException, IOException {

		final Collection<TranslationSerialization> records = Lists.newArrayList();

		final Iterable<String> selectedLanguages = Arrays.asList(Strings.split(_languages, ','));

		if (type.equalsIgnoreCase(ALL)) {
			for (final Sections section : Sections.values()) {
				Iterables.addAll(records, serialize(section.name(), sorters, activeOnly, selectedLanguages));
			}
		} else {
			Iterables.addAll(records, serialize(type, sorters, activeOnly, selectedLanguages));
		}
		final String fileName = String.format("%s.csv", type);
		final File outputFile = new File(fileName);
		final Iterable<Map<String, Object>> rows = TO_MAP.apply(records);

		final DataHandler dataHandler = DefaultCsvExporter.newInstance() //
				.withRecords(rows) //
				.withFile(outputFile) //
				.withHeaders(initHeaders(selectedLanguages)) //
				.withSeparator(separator) //
				.build() //
				.write();

		return dataHandler;
	}

	@JSONExported
	@Admin
	public JsonResponse importCsv(@Parameter(value = FILE) final FileItem file, //
			@Parameter(value = SEPARATOR, required = false) final String separator //
	) throws JSONException, IOException {

		final DataHandler input = new DataHandler(FileItemDataSource.of(file));

		final Iterable<CsvTranslationRecord> records = DefaultCsvImporter.newInstance() //
				.withDataHandler(input) //
				.withSeparator(separator) //
				.build() //
				.read();

		final ErrorListener listener = new ErrorListener() {

			private final Map<TranslationSerialization, Throwable> failures = Maps.newHashMap();

			@Override
			public void handleError(final TranslationSerialization input, final Throwable throwable) {
				failures.put(input, throwable);
			}

			@Override
			public Map<TranslationSerialization, Throwable> getFailures() {
				return failures;
			}

		};
		for (final CsvTranslationRecord record : records) {
			final RecordDeserializer importer = SafeRecordDeserializer //
					.of(a(DefaultRecordDeserializer.newInstance() //
							.withRecord(record) //
							.withNotifier(LoggingErrorNotifier.of(THROWS_EXCEPTION)))) //
					.withErrorListener(listener);
			final TranslationObject translationObject = importer.deserialize();
			if (!translationObject.isValid()) {
				continue;
			}
			translationLogic().update(translationObject);
		}
		final Map<String, Object> response = Maps.newHashMap();
		response.put(FAILURES, TO_CLIENT.apply(listener.getFailures()));
		return JsonResponse.success(response);
	}

	private Iterable<TranslationSerialization> serialize(final String type, final JSONArray sorters,
			final boolean activeOnly, final Iterable<String> selectedLanguages) {
		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withOutput(Output.CSV) //
				.withActiveOnly(activeOnly) //
				.withAuthLogic(authLogic()) //
				.withDataAccessLogic(userDataAccessLogic()) //
				.withFilterLogic(filterLogic()) //
				.withLookupStore(lookupStore()) //
				.withMenuLogic(menuLogic()) //
				.withReportStore(reportStore()) //
				.withSorters(sorters) //
				.withTranslationLogic(translationLogic()) //
				.withType(type) //
				.withViewLogic(viewLogic()) //
				.withSelectedLanguages(selectedLanguages) //
				.build();

		final TranslationSectionSerializer serializer = factory.createSerializer();
		final Iterable<TranslationSerialization> records = serializer.serialize();
		return records;
	}

	private String[] initHeaders(final Iterable<String> selectedLanguages) {
		Collection<String> languagesToExport = Lists.newArrayList(selectedLanguages);
		Iterables.retainAll(languagesToExport, Lists.newArrayList(setupFacade().getEnabledLanguages()));
		final Collection<String> allHeaders = Lists.newArrayList(commonHeaders);
		for (final String lang : languagesToExport) {
			allHeaders.add(lang);
		}
		String[] csvHeader = new String[allHeaders.size()];
		csvHeader = allHeaders.toArray(new String[0]);
		return csvHeader;
	}

	@JSONExported
	@Admin
	public JsonResponse readAll( //
			@Parameter(value = TYPE) final String type, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = ACTIVE, required = false) final boolean activeOnly //
	) throws JSONException {

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withActiveOnly(activeOnly) //
				.withAuthLogic(authLogic()) //
				.withDataAccessLogic(userDataAccessLogic()) //
				.withFilterLogic(filterLogic()) //
				.withLookupStore(lookupStore()) //
				.withMenuLogic(menuLogic()) //
				.withOutput(Output.TABLE) //
				.withReportStore(reportStore()).withSorters(sorters) //
				.withTranslationLogic(translationLogic()) //
				.withType(type) //
				.withViewLogic(viewLogic()) //
				.build();

		final TranslationSectionSerializer serializer = factory.createSerializer();
		return JsonResponse.success(serializer.serialize());
	}

}