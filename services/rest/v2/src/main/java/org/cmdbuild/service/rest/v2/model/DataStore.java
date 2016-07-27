package org.cmdbuild.service.rest.v2.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class DataStore extends ModelWithStringId {

	DataStore() {
		// package visibility
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DataStore)) {
			return false;
		}

		final DataStore other = DataStore.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.toHashCode();
	}

}
