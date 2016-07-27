package org.cmdbuild.logic.translation.object;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.BaseTranslation;
import org.cmdbuild.logic.translation.TranslationObjectVisitor;

public class WidgetLabel extends BaseTranslation {

	//FIXME: add owner
	
	private WidgetLabel(final Builder builder) {
		this.setName(builder.name);
		this.setTranslations(builder.translations);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<WidgetLabel> {

		private String name;
		private Map<String, String> translations;

		private Builder() {
		}

		@Override
		public WidgetLabel build() {
			validate();
			return new WidgetLabel(this);
		}

		public Builder withClassName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

		private void validate() {
			Validate.notBlank(name);
		}

	}

}
