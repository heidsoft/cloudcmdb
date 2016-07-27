package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.object.FilterDescription;
import org.cmdbuild.logic.translation.object.ViewDescription;

import com.google.common.collect.Maps;

public enum FilterConverter implements Converter {

	DESCRIPTION(description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public FilterDescription create() {
			validate();
			final FilterDescription.Builder builder = FilterDescription //
					.newInstance() //
					.withName(name);

			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}
	},

	UNDEFINED(undefined()) {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public ViewDescription create() {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;

	private static String name;
	private static Map<String, String> translations = Maps.newHashMap();

	private static final String DESCRIPTION_FIELD = "description";
	private static final String UNDEFINED_FIELD = "undefined";

	@Override
	public Converter withIdentifier(final String identifier) {
		name = identifier;
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

	private static void validate() {
		Validate.notBlank(name);
	}

	public static String description() {
		return DESCRIPTION_FIELD;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private FilterConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static FilterConverter of(final String value) {
		for (final FilterConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
