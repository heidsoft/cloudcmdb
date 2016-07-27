package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.NullTranslationObject;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;
import org.cmdbuild.logic.translation.object.DomainAttributeDescription;

import com.google.common.collect.Maps;

public enum AttributeConverter implements Converter {

	CLASSATTRIBUTE_DESCRIPTION(forClass(), description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create() {
			final org.cmdbuild.logic.translation.object.ClassAttributeDescription.Builder builder = ClassAttributeDescription
					.newInstance() //
					.withClassname(parentName) //
					.withAttributename(attributeName);
			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}
	},

	DOMAINATTRIBUTE_DESCRIPTION(forDomain(), description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create() {
			final DomainAttributeDescription.Builder builder = DomainAttributeDescription.newInstance() //
					.withDomainName(parentName) //
					.withAttributeName(attributeName);
			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}

	},

	CLASSATTRIBUTE_GROUP(forClass(), group()) {

		@Override
		public TranslationObject create() {
			final org.cmdbuild.logic.translation.object.ClassAttributeGroup.Builder builder = ClassAttributeGroup
					.newInstance() //
					.withClassname(parentName) //
					.withAttributename(attributeName);
			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();

		}

		@Override
		public boolean isValid() {
			return true;
		}

	},

	DOMAINATTRIBUTE_GROUP(forDomain(), group()) {

		@Override
		public TranslationObject create() {
			return new NullTranslationObject();
		}

		@Override
		public boolean isValid() {
			return true;
		}

	},

	UNDEFINED(undefined(), group()) {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public TranslationObject create() {
			throw new UnsupportedOperationException();
		}

	};

	private static final String CLASS = "class";
	private static final String DOMAIN = "domain";
	private static final String DESCRIPTION = "description";
	private static final String GROUP = "group";
	private static final String UNDEFINED_FIELD = "undefined";

	private final String fieldName;
	private final String entryType;

	private static String parentName;
	private static String attributeName;
	private static Map<String, String> translations = Maps.newHashMap();

	@Override
	public Converter withIdentifier(final String identifier) {
		attributeName = identifier;
		return this;
	}

	@Override
	public Converter withOwner(final String parentIdentifier) {
		parentName = parentIdentifier;
		return this;
	}

	@Override
	public Converter withTranslations(final Map<String, String> map) {
		translations = map;
		return this;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	public static String description() {
		return DESCRIPTION;
	}

	public static String group() {
		return GROUP;
	}

	public static String forClass() {
		return CLASS;
	}

	public static String forDomain() {
		return DOMAIN;
	}

	private AttributeConverter(final String entryType, final String fieldName) {
		this.entryType = entryType;
		this.fieldName = fieldName;
	}

	public static AttributeConverter of(final String entryType, final String value) {
		for (final AttributeConverter element : values()) {
			if (element.entryType.equalsIgnoreCase(entryType) && element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

	public String fieldName() {
		return fieldName;
	}

	public String entryType() {
		return entryType;
	}
}
