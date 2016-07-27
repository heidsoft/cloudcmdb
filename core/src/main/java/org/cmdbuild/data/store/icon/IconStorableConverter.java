package org.cmdbuild.data.store.icon;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

import com.google.common.collect.Maps;

public class IconStorableConverter extends BaseStorableConverter<Icon> {

	private static class StorableIconImpl implements Icon {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<Icon> {

			private Long id;
			private String element;
			private String path;

			private Builder() {
				// use factory method
			}

			@Override
			public Icon build() {
				validate();
				return new StorableIconImpl(this);
			}

			private void validate() {
				Validate.isTrue(isNotBlank(element), "invalid element");
				Validate.isTrue(isNotBlank(path), "invalid path");
			}

			@Override
			public String toString() {
				return reflectionToString(this, SHORT_PREFIX_STYLE);
			}

			public Builder withId(final Long value) {
				this.id = value;
				return this;
			}

			public Builder withElement(final String value) {
				this.element = value;
				return this;
			}

			public Builder withPath(final String value) {
				this.path = value;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final Long id;
		private final String element;
		private final String path;

		private StorableIconImpl(final Builder builder) {
			this.id = builder.id;
			this.element = builder.element;
			this.path = builder.path;
		}

		@Override
		public String getIdentifier() {
			return id.toString();
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public String getElement() {
			return element;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Icon)) {
				return false;
			}

			final Icon other = Icon.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getElement(), other.getElement()) //
					.append(this.getPath(), other.getPath()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getId()) //
					.append(getElement()) //
					.append(getPath()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	private static final String EMAIL_ACCOUNT = "_Icon";

	private static final String ELEMENT = "Element";
	private static final String PATH = "Path";

	@Override
	public String getClassName() {
		return EMAIL_ACCOUNT;
	}

	@Override
	public Icon convert(final CMCard card) {
		return StorableIconImpl.newInstance() //
				.withId(card.getId()) //
				.withElement(card.get(ELEMENT, String.class)) //
				.withPath(card.get(PATH, String.class)) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final Icon storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(ELEMENT, storable.getElement());
		values.put(PATH, storable.getPath());
		return values;
	}

}
