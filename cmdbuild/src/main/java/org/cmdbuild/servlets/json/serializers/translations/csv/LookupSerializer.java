package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public class LookupSerializer extends DefaultElementSerializer {

	private final Lookup aLookupValue;
	private final LookupType type;

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public Collection<? extends CsvTranslationRecord> serialize() {
		final TranslatableElement element = TranslatableElement.LOOKUP_VALUE;
		return serializeFields(type.name, aLookupValue.getTranslationUuid(), element);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<LookupSerializer> {

		private DataAccessLogic dataLogic;
		private Iterable<String> selectedLanguages;
		private LookupStore lookupStore;
		private TranslationLogic translationLogic;
		private LookupType type;
		private Lookup value;

		@Override
		public LookupSerializer build() {
			return new LookupSerializer(this);
		}

		public Builder withDataAccessLogic(final DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public Builder withSelectedLanguages(final Iterable<String> selectedLanguages) {
			this.selectedLanguages = selectedLanguages;
			return this;
		}

		public Builder withLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
			return this;
		}

		public Builder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

		public Builder withLookupType(final LookupType type) {
			this.type = type;
			return this;
		}

		public Builder withLookupValue(final Lookup value) {
			this.value = value;
			return this;
		}

	}

	private LookupSerializer(final Builder builder) {
		super.dataLogic = builder.dataLogic;
		super.selectedLanguages = builder.selectedLanguages;
		super.translationLogic = builder.translationLogic;
		super.lookupStore = builder.lookupStore;
		this.type = builder.type;
		this.aLookupValue = builder.value;
	}

}
