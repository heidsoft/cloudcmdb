package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public class DomainSerializer extends DefaultElementSerializer {

	private final CMDomain theDomain;

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public Collection<? extends CsvTranslationRecord> serialize() {
		final String domainName = theDomain.getName();
		final TranslatableElement element = TranslatableElement.DOMAIN;
		final String NO_OWNER = EMPTY;
		return serializeFields(NO_OWNER, domainName, element);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DomainSerializer> {

		private DataAccessLogic dataLogic;
		private Iterable<String> selectedLanguages;
		private TranslationLogic translationLogic;
		public CMDomain theDomain;

		@Override
		public DomainSerializer build() {
			return new DomainSerializer(this);
		}

		public Builder withDomain(final CMDomain theDomain) {
			this.theDomain = theDomain;
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

	private DomainSerializer(final Builder builder) {
		super.dataLogic = builder.dataLogic;
		super.selectedLanguages = builder.selectedLanguages;
		super.translationLogic = builder.translationLogic;
		this.theDomain = builder.theDomain;
	}

}
