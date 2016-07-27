package org.cmdbuild.logic.translation.object;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.BaseTranslation;
import org.cmdbuild.logic.translation.TranslationObjectVisitor;

public class DomainAttributeDescription extends BaseTranslation {

	private String domainName;

	private DomainAttributeDescription(final Builder builder) {
		this.setDomainName(builder.domainName);
		this.setName(builder.attributeName);
		this.setTranslations(builder.translations);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	private void setDomainName(final String className) {
		this.domainName = className;
	}

	public String getDomainName() {
		return domainName;
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DomainAttributeDescription> {

		private String domainName;
		private String attributeName;
		private Map<String, String> translations;

		private Builder() {
		}

		@Override
		public DomainAttributeDescription build() {
			validate();
			return new DomainAttributeDescription(this);
		}

		public Builder withDomainName(final String className) {
			this.domainName = className;
			return this;
		}

		public Builder withAttributeName(final String attributeName) {
			this.attributeName = attributeName;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

		private void validate() {
			Validate.notBlank(domainName);
			Validate.notBlank(attributeName);
		}

	}

}
