package org.cmdbuild.services.template.engine;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.services.template.store.TemplateRepository;

public class DatabaseEngine implements Engine {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DatabaseEngine> {

		private TemplateRepository templateRepository;

		private Builder() {
			// use factory method
		}

		@Override
		public DatabaseEngine build() {
			validate();
			return new DatabaseEngine(this);
		}

		private void validate() {
			Validate.notNull(templateRepository, "missing '{}'", TemplateRepository.class);
		}

		public Builder withTemplateRepository(final TemplateRepository templateRepository) {
			this.templateRepository = templateRepository;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final TemplateRepository templateRepository;

	private DatabaseEngine(final Builder builder) {
		this.templateRepository = builder.templateRepository;
	}

	@Override
	public Object eval(final String expression) {
		return templateRepository.getTemplate(expression);
	}

}
