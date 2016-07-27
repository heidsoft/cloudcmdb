package org.cmdbuild.services.sync.store;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SimpleAttribute extends AbstractAttribute {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<SimpleAttribute> {

		private String name;
		private Boolean isKey;

		private Builder() {
			// use factory method
		}

		@Override
		public SimpleAttribute build() {
			validate();
			return new SimpleAttribute(this);
		}

		private void validate() {
			Validate.notBlank(name, "invalid name");
			isKey = defaultIfNull(isKey, false);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withKeyStatus(final Boolean isKey) {
			this.isKey = isKey;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String name;
	private final boolean isKey;

	private SimpleAttribute(final Builder builder) {
		this.name = builder.name;
		this.isKey = builder.isKey;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isKey() {
		return isKey;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof SimpleAttribute)) {
			return false;
		}
		final SimpleAttribute other = SimpleAttribute.class.cast(obj);
		return name.equals(other.name);
	}

	@Override
	protected int doHashCode() {
		return name.hashCode();
	}

	@Override
	protected String doToString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("name", name) //
				.append("key", isKey) //
				.toString();
	}

}
