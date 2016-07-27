package org.cmdbuild.workflow.xpdl;

import static org.apache.commons.lang3.Validate.notEmpty;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CMActivityMetadata {

	private final String name;
	private final String value;

	public CMActivityMetadata(final String name, final String value) {
		this.name = notEmpty(name, "name cannot be empty");
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof CMActivityMetadata)) {
			return false;
		}

		final CMActivityMetadata other = CMActivityMetadata.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.value, other.value) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(name) //
				.append(value) //
				.toHashCode();
	}

}
