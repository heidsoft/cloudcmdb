package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.IDENTIFIER;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.KEY_SEPARATOR;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.commonHeaders;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.createConverter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.axis.utils.StringUtils;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultRecordDeserializer implements RecordDeserializer {

	private static final Logger logger = Log.JSONRPC;
	private static final Marker marker = MarkerFactory.getMarker(DefaultRecordDeserializer.class.getName());

	private String field = EMPTY;
	private String identifier = EMPTY;
	private final ErrorNotifier notifier;
	private String owner = EMPTY;
	private final CsvTranslationRecord record;
	private final Map<String, String> translations = Maps.newHashMap();
	private String type = EMPTY;

	public DefaultRecordDeserializer(final Builder builder) {
		this.record = builder.record;
		this.notifier = builder.notifier;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<RecordDeserializer> {

		private CsvTranslationRecord record;
		private ErrorNotifier notifier;

		@Override
		public RecordDeserializer build() {
			return new DefaultRecordDeserializer(this);
		}

		public Builder withNotifier(final ErrorNotifier notifier) {
			this.notifier = notifier;
			return this;
		}

		public Builder withRecord(final CsvTranslationRecord record) {
			this.record = record;
			return this;
		}

	}

	@Override
	public TranslationSerialization getInput() {
		return record;
	}

	@Override
	public TranslationObject deserialize() {
		logger.info(marker, "parsing record '{}'", record.toString());
		final String key = record.get(IDENTIFIER);
		unpack(key);
		logger.debug(marker, "identifier deserialized to type: '{}', owner: '{}', identifier: '{}', field: '{}'", type,
				owner, identifier, field);

		final TranslatableElement element = TO_ELEMENT_TYPE.apply(type);
		if (element.equals(TranslatableElement.UNDEFINED)) {
			notifier.unsupportedType(type);
		}
		if (!Iterables.contains(element.allowedFields(), field)) {
			notifier.unsupportedField(field);
		}

		final Converter converter = createConverter(type, field);
		if (!converter.isValid()) {
			notifier.invalidConverter();
		}
		extractTranslations(record);
		logger.debug(marker, "translations: '{}'", translations);

		final TranslationObject translationObject = converter //
				.withOwner(owner) //
				.withIdentifier(identifier) //
				.withTranslations(translations) //
				.create();

		return translationObject;
	}

	private void extractTranslations(final CsvTranslationRecord record) {
		final Set<String> headers = record.getKeySet();
		final Collection<String> languages = Lists.newArrayList();
		Iterables.addAll(languages, headers);
		Iterables.removeAll(languages, commonHeaders);
		for (final String language : languages) {
			translations.put(language, record.get(language));
		}
	}

	private void unpack(final String key) {
		validate(key);
		final String[] parts = StringUtils.split(key, KEY_SEPARATOR);
		type = parts[0];
		if (shouldHaveOwnerField(key)) {
			owner = parts[1];
			identifier = parts[2];
			field = parts[3];
		} else {
			identifier = parts[1];
			field = parts[2];
		}
	}

	private static boolean shouldHaveOwnerField(final String key) {
		return StringUtils.split(key, KEY_SEPARATOR).length == 4;
	}

	private void validate(final String key) {
		if (isBlank(key)) {
			notifier.unsupportedIdentifier(key);
		}
		final int fields = StringUtils.split(key, KEY_SEPARATOR).length;
		if (fields != 3 && fields != 4) {
			notifier.unsupportedIdentifier(key);
		}
	}

	private static final Function<String, TranslatableElement> TO_ELEMENT_TYPE = new Function<String, TranslatableElement>() {
		@Override
		public TranslatableElement apply(final String input) {
			return TranslatableElement.of(input);
		}
	};

}
