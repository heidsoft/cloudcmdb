package org.cmdbuild.service.rest.v1.cxf.serialization;

import static org.cmdbuild.service.rest.v1.model.Models.newClassWithBasicDetails;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.v1.model.ClassWithBasicDetails;

import com.google.common.base.Function;

public class ToSimpleClassDetail implements Function<CMClass, ClassWithBasicDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToSimpleClassDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToSimpleClassDetail build() {
			return new ToSimpleClassDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final String MISSING_PARENT = null;

	private ToSimpleClassDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public ClassWithBasicDetails apply(final CMClass input) {
		final CMClass parent = input.getParent();
		return newClassWithBasicDetails() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.withParent((parent == null) ? MISSING_PARENT : parent.getName()) //
				.thatIsPrototype(input.isSuperclass()) //
				.build();
	}

}
