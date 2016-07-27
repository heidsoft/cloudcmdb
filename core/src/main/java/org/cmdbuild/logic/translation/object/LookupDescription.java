package org.cmdbuild.logic.translation.object;

import java.util.Map;

import org.cmdbuild.logic.translation.BaseTranslation;
import org.cmdbuild.logic.translation.TranslationObjectVisitor;

public class LookupDescription extends BaseTranslation {

	private LookupDescription(final Builder builder) {
		this.setName(builder.uuid);
		this.setTranslations(builder.translations);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<LookupDescription> {

		private String uuid;
		private Map<String, String> translations;

		private Builder() {
		}

		@Override
		public LookupDescription build() {
			return new LookupDescription(this);
		}

		public Builder withUuid(final String uuid) {
			this.uuid = uuid;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

	}

}
