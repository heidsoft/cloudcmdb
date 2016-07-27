package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.object.ClassDescription;

import com.google.common.collect.Maps;

public enum ClassConverter implements Converter {

	DESCRIPTION(description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public ClassDescription create() {
			final org.cmdbuild.logic.translation.object.ClassDescription.Builder builder = ClassDescription
					.newInstance() //
					.withClassName(className);

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
		public ClassDescription create() {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;

	private static String className;
	private static Map<String, String> translations = Maps.newHashMap();

	private static final String DESCRIPTION_FIELD = "description";
	private static final String UNDEFINED_FIELD = "undefined";

	@Override
	public Converter withIdentifier(final String identifier) {
		className = identifier;
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

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private ClassConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static ClassConverter of(final String value) {
		for (final ClassConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
