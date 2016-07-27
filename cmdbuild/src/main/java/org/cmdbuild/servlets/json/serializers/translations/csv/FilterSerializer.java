package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.NO_OWNER;

import java.util.Collection;

import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.logic.filter.FilterLogic.Filter;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public class FilterSerializer extends DefaultElementSerializer {

	private final Filter theFilter;

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public Collection<? extends CsvTranslationRecord> serialize() {
		final String filterName = theFilter.getName();
		final TranslatableElement element = TranslatableElement.FILTER;
		return serializeFields(NO_OWNER, filterName, element);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<FilterSerializer> {

		private Iterable<String> selectedLanguages;
		private TranslationLogic translationLogic;
		public Filter theFilter;
		public FilterLogic filterLogic;

		@Override
		public FilterSerializer build() {
			return new FilterSerializer(this);
		}

		public Builder withFilter(final Filter theFilter) {
			this.theFilter = theFilter;
			return this;
		}

		public Builder withSelectedLanguages(final Iterable<String> selectedLanguages) {
			this.selectedLanguages = selectedLanguages;
			return this;
		}

		public Builder withFilterLogic(final FilterLogic filterLogic) {
			this.filterLogic = filterLogic;
			return this;
		}

		public Builder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

	}

	private FilterSerializer(final Builder builder) {
		super.filterLogic = builder.filterLogic;
		super.selectedLanguages = builder.selectedLanguages;
		super.translationLogic = builder.translationLogic;
		this.theFilter = builder.theFilter;
	}

}
