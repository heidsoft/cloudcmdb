package org.cmdbuild.service.rest.v2.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class FunctionWithFullDetails extends FunctionWithBasicDetails {

	FunctionWithFullDetails() {
		// package visibility
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof FunctionWithFullDetails)) {
			return false;
		}

		final FunctionWithFullDetails other = FunctionWithFullDetails.class.cast(obj);
		return super.doEquals(obj) && new EqualsBuilder() //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.toHashCode();
	}

}
