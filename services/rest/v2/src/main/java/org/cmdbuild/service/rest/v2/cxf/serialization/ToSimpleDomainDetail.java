package org.cmdbuild.service.rest.v2.cxf.serialization;

import static org.cmdbuild.service.rest.v2.model.Models.newDomainWithBasicDetails;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.service.rest.v2.model.DomainWithBasicDetails;

import com.google.common.base.Function;

public class ToSimpleDomainDetail implements Function<CMDomain, DomainWithBasicDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToSimpleDomainDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToSimpleDomainDetail build() {
			return new ToSimpleDomainDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToSimpleDomainDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public DomainWithBasicDetails apply(final CMDomain input) {
		return newDomainWithBasicDetails() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.build();
	}

}
