package org.cmdbuild.data.store.metadata;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MetadataImpl implements Metadata {

	public static MetadataImpl of(final String name) {
		return of(name, null);
	}

	public static MetadataImpl of(final String name, final String value) {
		return new MetadataImpl(name, value);
	}

	private final String name;
	private final String value;

	private MetadataImpl(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getIdentifier() {
		return name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String value() {
		return value;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(this.name) //
				.append(this.value) //
				.toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof MetadataImpl)) {
			return false;
		}
		final MetadataImpl other = MetadataImpl.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.value, other.value) //
				.isEquals();
	}

	@Override
	public String toString() {
		return reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
