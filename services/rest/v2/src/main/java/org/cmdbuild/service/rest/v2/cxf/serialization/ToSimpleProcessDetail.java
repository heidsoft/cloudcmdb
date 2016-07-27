package org.cmdbuild.service.rest.v2.cxf.serialization;

import static org.cmdbuild.service.rest.v2.model.Models.newProcessWithBasicDetails;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.v2.model.ProcessWithBasicDetails;

import com.google.common.base.Function;

public class ToSimpleProcessDetail implements Function<CMClass, ProcessWithBasicDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToSimpleProcessDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToSimpleProcessDetail build() {
			return new ToSimpleProcessDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final String MISSING_PARENT = null;

	private ToSimpleProcessDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public ProcessWithBasicDetails apply(final CMClass input) {
		final CMClass parent = input.getParent();
		return newProcessWithBasicDetails() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.withParent((parent == null) ? MISSING_PARENT : parent.getName()) //
				.thatIsPrototype(input.isSuperclass()) //
				.build();
	}

}
