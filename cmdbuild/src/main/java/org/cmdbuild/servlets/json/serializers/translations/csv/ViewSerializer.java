package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.NO_OWNER;

import java.util.Collection;

import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public class ViewSerializer extends DefaultElementSerializer {

	private final View theView;

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public Collection<? extends CsvTranslationRecord> serialize() {
		final String viewName = theView.getName();
		final TranslatableElement element = TranslatableElement.VIEW;
		return serializeFields(NO_OWNER, viewName, element);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ViewSerializer> {

		private Iterable<String> selectedLanguages;
		private TranslationLogic translationLogic;
		public View theView;
		public ViewLogic viewLogic;

		@Override
		public ViewSerializer build() {
			return new ViewSerializer(this);
		}

		public Builder withView(final View theView) {
			this.theView = theView;
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

		public Builder withViewLogic(final ViewLogic viewLogic) {
			this.viewLogic = viewLogic;
			return this;
		}

	}

	private ViewSerializer(final Builder builder) {
		super.viewLogic = builder.viewLogic;
		super.selectedLanguages = builder.selectedLanguages;
		super.translationLogic = builder.translationLogic;
		this.theView = builder.theView;
	}

}
