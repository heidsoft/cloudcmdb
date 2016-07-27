package org.cmdbuild.servlets.json.util;

import static org.cmdbuild.servlets.json.CommunicationConstants.KEY;
import static org.cmdbuild.servlets.json.CommunicationConstants.VALUE;

import java.util.Map.Entry;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

public class JsonImmutableEntry implements Entry<String, Object> {

	public static JsonImmutableEntry of(final String key, final Object value) {
		return new JsonImmutableEntry(key, value);
	}

	private final String key;
	private final Object value;

	private JsonImmutableEntry(final String key, final Object value) {
		this.key = key;
		this.value = value;
	}

	@Override
	@JsonProperty(KEY)
	public String getKey() {
		return key;
	}

	@Override
	@JsonProperty(VALUE)
	public Object getValue() {
		return value;
	}

	@Override
	public Object setValue(final Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof JsonImmutableEntry)) {
			return false;
		}
		final JsonImmutableEntry other = JsonImmutableEntry.class.cast(obj);
		return new EqualsBuilder() //
				.append(key, other.key) //
				.append(value, other.value) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(key) //
				.append(value) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}