package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public class AttributeSerializer extends DefaultElementSerializer {

	private final CMAttribute theAttribute;

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public Collection<? extends CsvTranslationRecord> serialize() {
		final String attributeName = theAttribute.getName();
		final String className = theAttribute.getOwner().getName();
		final TranslatableElement element = TranslatableElement.ATTRIBUTECLASS;
		return serializeFields(className, attributeName, element);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AttributeSerializer> {

		private TranslationLogic translationLogic;
		private Iterable<String> selectedLanguages;
		public CMAttribute theAttribute;
		private DataAccessLogic dataLogic;

		@Override
		public AttributeSerializer build() {
			return new AttributeSerializer(this);
		}

		public Builder withAttribute(final CMAttribute theAttribute) {
			this.theAttribute = theAttribute;
			return this;
		}

		public Builder withDataAccessLogic(final DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public Builder withSelectedLanguages(final Iterable<String> selectedLanguages) {
			this.selectedLanguages = selectedLanguages;
			return this;
		}

		public Builder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

	}

	private AttributeSerializer(final Builder builder) {
		super.dataLogic = builder.dataLogic;
		super.selectedLanguages = builder.selectedLanguages;
		super.translationLogic = builder.translationLogic;
		this.theAttribute = builder.theAttribute;
	}

}
