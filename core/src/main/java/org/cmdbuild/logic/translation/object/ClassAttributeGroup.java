package org.cmdbuild.logic.translation.object;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.BaseTranslation;
import org.cmdbuild.logic.translation.TranslationObjectVisitor;

public class ClassAttributeGroup extends BaseTranslation {

	private String className;

	private ClassAttributeGroup(final Builder builder) {
		this.setClassName(builder.className);
		this.setName(builder.attributeName);
		this.setTranslations(builder.translations);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	private void setClassName(final String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ClassAttributeGroup> {

		private String className;
		private String attributeName;
		private Map<String, String> translations;

		private Builder() {
		}

		@Override
		public ClassAttributeGroup build() {
			validate();
			return new ClassAttributeGroup(this);
		}

		public Builder withClassname(final String className) {
			this.className = className;
			return this;
		}

		public Builder withAttributename(final String attributeName) {
			this.attributeName = attributeName;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

		private void validate() {
			Validate.notBlank(className);
			Validate.notBlank(attributeName);
		}

	}

}
