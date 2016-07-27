package org.cmdbuild.service.rest.v1.cxf.serialization;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessActivityWithBasicDetails;

import org.cmdbuild.service.rest.v1.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.workflow.CMActivity;

public class ToProcessActivityWithBasicDetailsFromCMActivity extends ToProcessActivityWithBasicDetails<CMActivity> {

	public static class Builder implements
			org.apache.commons.lang3.builder.Builder<ToProcessActivityWithBasicDetailsFromCMActivity> {

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessActivityWithBasicDetailsFromCMActivity build() {
			validate();
			return new ToProcessActivityWithBasicDetailsFromCMActivity(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToProcessActivityWithBasicDetailsFromCMActivity(final Builder builder) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessActivityWithBasicDetails apply(final CMActivity input) {
		return newProcessActivityWithBasicDetails() //
				.withId(input.getId()) //
				.withWritableStatus(true) //
				.withDescription(safeDescriptionOf(input)) //
				.build();
	}

	private String safeDescriptionOf(final CMActivity input) {
		try {
			return input.getDescription();
		} catch (final Exception e) {
			return EMPTY;
		}
	}

}
