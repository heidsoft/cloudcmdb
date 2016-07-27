package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.object.LookupDescription;

import com.google.common.collect.Maps;

public enum LookupConverter implements Converter {

	DESCRIPTION(description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public LookupDescription create() {
			final org.cmdbuild.logic.translation.object.LookupDescription.Builder builder = LookupDescription
					.newInstance() //
					.withUuid(uuid);

			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}
	},

	UNDEFINED("undefined") {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public LookupDescription create() {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;

	private static String uuid;
	private static Map<String, String> translations = Maps.newHashMap();
	private static final String DESCRIPTION_FIELD = "description";

	@Override
	public Converter withIdentifier(final String identifier) {
		uuid = identifier;
		return this;
	}

	@Override
	public Converter withOwner(final String parentIdentifier) {
		return this;
	}

	@Override
	public Converter withTranslations(final Map<String, String> map) {
		translations = map;
		return this;
	}

	public static String description() {
		return DESCRIPTION_FIELD;
	}

	private LookupConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static LookupConverter of(final String value) {
		for (final LookupConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
