package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.object.WidgetLabel;

import com.google.common.collect.Maps;

public enum WidgetConverter implements Converter {

	LABEL(label()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public WidgetLabel create() {
			final WidgetLabel.Builder builder = WidgetLabel //
					.newInstance() //
					.withClassName(name);

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
		public WidgetLabel create() {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;

	private static String name;
	private static Map<String, String> translations = Maps.newHashMap();

	private static final String LABEL_FIELD = "buttonlabel";
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

	public static String label() {
		return LABEL_FIELD;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private WidgetConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static WidgetConverter of(final String value) {
		for (final WidgetConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
