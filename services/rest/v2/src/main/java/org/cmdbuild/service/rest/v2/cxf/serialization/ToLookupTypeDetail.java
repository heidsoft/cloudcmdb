package org.cmdbuild.service.rest.v2.cxf.serialization;

import static org.cmdbuild.service.rest.v2.model.Models.newLookupTypeDetail;

import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.service.rest.v2.model.LookupTypeDetail;

import com.google.common.base.Function;

public class ToLookupTypeDetail implements Function<LookupType, LookupTypeDetail> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToLookupTypeDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToLookupTypeDetail build() {
			return new ToLookupTypeDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToLookupTypeDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public LookupTypeDetail apply(final LookupType input) {
		return newLookupTypeDetail() //
				.withId(input.name) //
				.withName(input.name) //
				.withParent(input.parent) //
				.build();
	}

}
