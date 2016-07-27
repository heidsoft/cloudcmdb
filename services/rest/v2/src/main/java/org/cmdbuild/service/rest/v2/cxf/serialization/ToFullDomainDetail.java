package org.cmdbuild.service.rest.v2.cxf.serialization;

import static org.cmdbuild.service.rest.v2.model.Models.newDomainWithFullDetails;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.v2.model.DomainWithFullDetails;

import com.google.common.base.Function;

public class ToFullDomainDetail implements Function<CMDomain, DomainWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToFullDomainDetail> {

		private DataAccessLogic dataAccessLogic;

		private Builder() {
			// use static method
		}

		@Override
		public ToFullDomainDetail build() {
			validate();
			return new ToFullDomainDetail(this);
		}

		private void validate() {
			Validate.notNull(dataAccessLogic, "missing '%s'", DataAccessLogic.class);
		}

		public Builder withDataAccessLogic(final DataAccessLogic dataAccessLogic) {
			this.dataAccessLogic = dataAccessLogic;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final DataAccessLogic dataAccessLogic;

	private ToFullDomainDetail(final Builder builder) {
		this.dataAccessLogic = builder.dataAccessLogic;
	}

	@Override
	public DomainWithFullDetails apply(final CMDomain input) {
		return newDomainWithFullDetails() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.withSource(input.getClass1().getName()) //
				.withSourceProcess(dataAccessLogic.isProcess(input.getClass1())) //
				.withDestination(input.getClass2().getName()) //
				.withDestinationProcess(dataAccessLogic.isProcess(input.getClass2())) //
				.withCardinality(input.getCardinality()) //
				.withDescriptionDirect(input.getDescription1()) //
				.withDescriptionInverse(input.getDescription2()) //
				.withDescriptionMasterDetail(input.getMasterDetailDescription()) //
				.withActive(input.isActive()) //
				.build();
	}

}
