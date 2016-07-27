package org.cmdbuild.model.data;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.data.store.Storable;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class Card implements Storable {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<Card> {

		private Long id;
		private CMClass type;
		private String className;
		private DateTime begin;
		private DateTime end;
		private String user;
		private Map<String, Object> attributes = Maps.newHashMap();

		public Builder clone(final Card card) {
			this.id = card.id;
			this.className = card.className;
			this.begin = card.begin;
			this.end = card.end;
			this.user = card.user;
			this.attributes = card.attributes;
			this.type = card.type;
			return this;
		}

		public Builder() {
		}

		public Builder(final CMClass type) {
			this.type = type;
			this.className = type.getName();
		}

		public Builder withId(final Long value) {
			this.id = value;
			return this;
		}

		public Builder withClassName(final String value) {
			this.className = value;
			return this;
		}

		public Builder withBeginDate(final DateTime value) {
			this.begin = value;
			return this;
		}

		public Builder withEndDate(final DateTime value) {
			this.end = value;
			return this;
		}

		public Builder withUser(final String value) {
			this.user = value;
			return this;
		}

		public Builder clearAttributes() {
			this.attributes.clear();
			return this;
		}

		public Builder withAttribute(final String key, final Object value) {
			this.attributes.put(key, value);
			return this;
		}

		public Builder withAllAttributes(final Map<String, ? extends Object> values) {
			this.attributes.putAll(values);
			return this;
		}

		public Builder withAllAttributes(final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
			for (final Map.Entry<String, ? extends Object> entry : values) {
				this.attributes.put(entry.getKey(), entry.getValue());
			}
			return this;
		}

		@Override
		public Card build() {
			Validate.isTrue(isNotBlank(className));
			return new Card(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static Builder newInstance(final CMClass entryType) {
		return new Builder(entryType);
	}

	private final Long id;
	private final CMClass type;
	private final String className;
	private final DateTime begin;
	private final DateTime end;
	private final String user;
	private final Map<String, Object> attributes;

	private final transient String toString;

	protected Card(final Builder builder) {
		this.id = builder.id;
		this.type = builder.type;
		this.className = builder.className;
		this.begin = builder.begin;
		this.end = builder.end;
		this.user = builder.user;
		this.attributes = builder.attributes;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public String getIdentifier() {
		return getId().toString();
	}

	@Override
	public String toString() {
		return toString;
	}

	public Long getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}

	/**
	 * @deprecated use {@link getType().getDescription()} instead.
	 */
	@Deprecated
	public String getClassDescription() {
		return getType() == null ? null : getType().getDescription();
	}

	/**
	 * @deprecated use {@link getType().getId()} instead.
	 */
	@Deprecated
	public Long getClassId() {
		return getType() == null ? null : getType().getId();
	}

	public DateTime getBeginDate() {
		return begin;
	}

	public DateTime getEndDate() {
		return end;
	}

	public String getUser() {
		return user;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Object getAttribute(final String key) {
		return attributes.get(key);
	}

	public CMClass getType() {
		return type;
	}

	public <T> T getAttribute(final String key, final Class<T> requiredType) {
		return requiredType.cast(attributes.get(key));
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(getType()) //
				.append(className) //
				.append(id) //
				.toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Card other = (Card) obj;
		return new EqualsBuilder() //
				.append(this.getType(), other.getType()) //
				.append(this.className, other.className) //
				.append(this.id, other.id) //
				.isEquals();
	}

}
