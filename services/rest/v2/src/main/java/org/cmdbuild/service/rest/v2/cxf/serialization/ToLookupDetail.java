package org.cmdbuild.service.rest.v2.cxf.serialization;

import static org.cmdbuild.service.rest.v2.model.Models.newLookupDetail;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.service.rest.v2.model.LookupDetail;

import com.google.common.base.Function;

public class ToLookupDetail implements Function<Lookup, LookupDetail> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToLookupDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToLookupDetail build() {
			return new ToLookupDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToLookupDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public LookupDetail apply(final Lookup lookup) {
		return newLookupDetail() //
				.withId(lookup.getId()) //
				.withCode(lookup.code()) //
				.withDescription(lookup.description()) //
				.withType(lookup.type().name) //
				.withNumber(Long.valueOf(lookup.number())) //
				.thatIsActive(lookup.active()) //
				.thatIsDefault(lookup.isDefault()) //
				.withParentId(lookup.parentId()) //
				.withParentType(lookup.type().parent) //
				.build();
	}

}
