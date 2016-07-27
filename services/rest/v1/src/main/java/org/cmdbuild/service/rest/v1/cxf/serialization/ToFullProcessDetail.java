package org.cmdbuild.service.rest.v1.cxf.serialization;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessStatus;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessWithFullDetails;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.v1.cxf.ProcessStatusHelper;
import org.cmdbuild.service.rest.v1.model.ProcessStatus;
import org.cmdbuild.service.rest.v1.model.ProcessWithFullDetails;

import com.google.common.base.Function;

public class ToFullProcessDetail implements Function<CMClass, ProcessWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToFullProcessDetail> {

		private ProcessStatusHelper processStatusHelper;

		private Builder() {
			// use static method
		}

		public Builder withLookupHelper(final ProcessStatusHelper processStatusHelper) {
			this.processStatusHelper = processStatusHelper;
			return this;
		}

		@Override
		public ToFullProcessDetail build() {
			return new ToFullProcessDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final Function<ProcessStatus, Long> PROCESS_STATUS_ID = new Function<ProcessStatus, Long>() {

		@Override
		public Long apply(final ProcessStatus input) {
			return input.getId();
		}

	};

	private static final ProcessStatus NULL = newProcessStatus().build();;

	private final ProcessStatusHelper processStatusHelper;

	private ToFullProcessDetail(final Builder builder) {
		this.processStatusHelper = builder.processStatusHelper;
	}

	@Override
	public ProcessWithFullDetails apply(final CMClass input) {
		final CMClass parent = input.getParent();
		return newProcessWithFullDetails() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.thatIsPrototype(input.isSuperclass()) //
				.withDescriptionAttributeName(input.getDescriptionAttributeName()) //
				.withStatuses(from(processStatusHelper.allValues()) //
						.transform(PROCESS_STATUS_ID)) //
				.withDefaultStatus(defaultIfNull(processStatusHelper.defaultValue().orNull(), NULL).getId()) //
				.withParent((parent == null) ? null : parent.getName()) //
				.build();
	}

}
