package org.cmdbuild.services.sync.store;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Ordering.natural;
import static org.cmdbuild.services.sync.store.Functions.toAttributeName;
import static org.cmdbuild.services.sync.store.Predicates.keyAttributes;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class CardEntry extends AbstractEntry {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<CardEntry> {

		private ClassType type;
		private final Map<String, Object> values = Maps.newHashMap();

		private Builder() {
			// use factory method
		}

		@Override
		public CardEntry build() {
			validate();
			return new CardEntry(this);
		}

		private void validate() {
			Validate.notNull(type, "missing '%s'", ClassType.class);
			final Iterable<String> keyAttributeNames = from(type.getAttributes()) //
					.filter(keyAttributes()) //
					.transform(toAttributeName());
			for (final String name : keyAttributeNames) {
				final Object value = values.get(name);
				Validate.notNull(value, "invalid key value '%s'", name);
				if (value instanceof String) {
					Validate.notBlank(String.class.cast(value), "invalid key string value '%s'", name);
				}
			}
		}

		public Builder withType(final ClassType type) {
			this.type = type;
			return this;
		}

		public Builder withValue(final String name, final Object value) {
			this.values.put(name, value);
			return this;
		}

		public Builder withValue(final Map.Entry<String, ? extends Object> value) {
			return withValue(value.getKey(), value.getValue());
		}

		public Builder withValues(final Iterable<? extends Map.Entry<String, ? extends Object>> values) {
			for (final Map.Entry<String, ? extends Object> value : values) {
				withValue(value);
			}
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static class KeyImpl extends AbstractKey {

		public static KeyImpl of(final CardEntry entry) {
			return new KeyImpl(entry);
		}

		private final CardEntry entry;

		private KeyImpl(final CardEntry entry) {
			this.entry = entry;
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof KeyImpl)) {
				return false;
			}
			final KeyImpl other = KeyImpl.class.cast(obj);
			if (!entry.getType().equals(other.entry.getType())) {
				return false;
			}
			final Iterable<String> keyAttributeNames = from(entry.getType().getAttributes()) //
					.filter(keyAttributes()) //
					.transform(toAttributeName());
			for (final String name : keyAttributeNames) {
				final Object lhs = entry.getValue(name);
				final Object rhs = other.entry.getValue(name);
				final boolean equals = Objects.equal(lhs, rhs);
				if (!equals) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected int doHashCode() {
			final HashCodeBuilder builder = new HashCodeBuilder();

			final ClassType type = entry.getType();
			builder.append(type.getName());

			for (final String name : natural() //
					.sortedCopy(from(type.getAttributes()) //
							.filter(keyAttributes()) //
							.transform(toAttributeName()))) {
				builder.append(entry.getValue(name));
			}

			return builder.hashCode();
		}

		@Override
		protected String doToString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
					.append("entry", entry) //
					.toString();
		}
	}

	private final ClassType type;
	private final Map<String, Object> values;
	private final Key key;

	public CardEntry(final Builder builder) {
		this.type = builder.type;
		this.values = builder.values;
		this.key = KeyImpl.of(this);
	}

	@Override
	public ClassType getType() {
		return type;
	}

	@Override
	public Iterable<Map.Entry<String, Object>> getValues() {
		return values.entrySet();
	}

	@Override
	public Object getValue(final String name) {
		return values.get(name);
	}

	@Override
	public Key getKey() {
		return key;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CardEntry)) {
			return false;
		}
		final CardEntry other = CardEntry.class.cast(obj);
		return type.equals(other.type) && values.equals(other.values);
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(type) //
				.append(values) //
				.toHashCode();
	}

	@Override
	protected String doToString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("type", type) //
				.append("values", values) //
				.toString();
	}

}
