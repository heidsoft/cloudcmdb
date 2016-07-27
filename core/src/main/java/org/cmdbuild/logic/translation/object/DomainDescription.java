package org.cmdbuild.logic.translation.object;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.BaseTranslation;
import org.cmdbuild.logic.translation.TranslationObjectVisitor;

public class DomainDescription extends BaseTranslation {

	private DomainDescription(final Builder builder) {
		this.setName(builder.domainName);
		this.setTranslations(builder.translations);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DomainDescription> {

		private String domainName;
		private Map<String, String> translations;

		private Builder() {
		}

		@Override
		public DomainDescription build() {
			validate();
			return new DomainDescription(this);
		}

		public Builder withDomainName(final String domainName) {
			this.domainName = domainName;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

		private void validate() {
			Validate.notBlank(domainName);
		}

	}

}
