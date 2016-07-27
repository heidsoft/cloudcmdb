package org.cmdbuild.data.store.email;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Maps;

public class DefaultExtendedEmailTemplate extends ForwardingEmailTemplate implements ExtendedEmailTemplate {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DefaultExtendedEmailTemplate> {

		private EmailTemplate delegate;
		private final Map<String, String> variables = Maps.newHashMap();

		private Builder() {
			// use factory method
		}

		@Override
		public DefaultExtendedEmailTemplate build() {
			validate();
			return new DefaultExtendedEmailTemplate(this);
		}

		private void validate() {
			Validate.notNull(delegate, "missing '%s'", delegate);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		public Builder withDelegate(final EmailTemplate delegate) {
			this.delegate = delegate;
			return this;
		}

		public Builder withVariables(final Map<String, String> variables) {
			this.variables.putAll(variables);
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Map<String, String> variables;

	private final EmailTemplate delegate;

	private DefaultExtendedEmailTemplate(final Builder builder) {
		this.delegate = builder.delegate;
		this.variables = builder.variables;
	}

	@Override
	protected EmailTemplate delegate() {
		return delegate;
	}

	@Override
	public Map<String, String> getVariables() {
		return variables;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
