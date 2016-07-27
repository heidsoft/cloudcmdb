package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.object.InstanceName;
import org.cmdbuild.logic.translation.object.ViewDescription;

import com.google.common.collect.Maps;

public enum InstanceConverter implements Converter {

	NAME(nameField()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public InstanceName create() {
			validate();
			final InstanceName.Builder builder = InstanceName //
					.newInstance();

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

	private static Map<String, String> translations = Maps.newHashMap();

	private static final String NAME_FIELD = "instancename";
	private static final String UNDEFINED_FIELD = "undefined";

	@Override
	public Converter withIdentifier(final String identifier) {
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
		// nothing to do
	}

	public static String nameField() {
		return NAME_FIELD;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private InstanceConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static InstanceConverter of(final String value) {
		for (final InstanceConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
