package org.cmdbuild.service.rest.v2.cxf.serialization;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.model.Models.newProcessActivityWithBasicDetails;

import org.cmdbuild.service.rest.v2.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.workflow.user.UserActivityInstance;

public class ToProcessActivityWithBasicDetailsFromUserActivityInstance extends
		ToProcessActivityWithBasicDetails<UserActivityInstance> {

	public static class Builder implements
			org.apache.commons.lang3.builder.Builder<ToProcessActivityWithBasicDetailsFromUserActivityInstance> {

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessActivityWithBasicDetailsFromUserActivityInstance build() {
			validate();
			return new ToProcessActivityWithBasicDetailsFromUserActivityInstance(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToProcessActivityWithBasicDetailsFromUserActivityInstance(final Builder builder) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ProcessActivityWithBasicDetails apply(final UserActivityInstance input) {
		return newProcessActivityWithBasicDetails() //
				.withId(input.getId()) //
				.withWritableStatus(input.isWritable()) //
				.withDescription(safeDescriptionOf(input)) //
				.build();
	}

	private String safeDescriptionOf(final UserActivityInstance input) {
		try {
			return input.getDefinition().getDescription();
		} catch (final Exception e) {
			return EMPTY;
		}
	}

}
