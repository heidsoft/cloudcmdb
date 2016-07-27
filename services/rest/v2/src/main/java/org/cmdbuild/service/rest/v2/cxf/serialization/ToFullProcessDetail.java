package org.cmdbuild.service.rest.v2.cxf.serialization;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Ordering.from;
import static java.lang.Math.abs;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.dao.entrytype.Predicates.classOrder;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ASCENDING;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCENDING;
import static org.cmdbuild.service.rest.v2.model.Models.newAttributeOrder;
import static org.cmdbuild.service.rest.v2.model.Models.newProcessStatus;
import static org.cmdbuild.service.rest.v2.model.Models.newProcessWithFullDetails;

import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.v2.cxf.ProcessStatusHelper;
import org.cmdbuild.service.rest.v2.model.ClassWithFullDetails.AttributeOrder;
import org.cmdbuild.service.rest.v2.model.ProcessStatus;
import org.cmdbuild.service.rest.v2.model.ProcessWithFullDetails;

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
				.withDefaultOrder(orderOf(input)) //
				.withParent((parent == null) ? null : parent.getName()) //
				.build();
	}

	private static final Comparator<CMAttribute> BY_CLASS_ORDER = new Comparator<CMAttribute>() {

		@Override
		public int compare(final CMAttribute o1, final CMAttribute o2) {
			return ObjectUtils.compare(abs(o1.getClassOrder()), abs(o2.getClassOrder()));
		}

	};

	private Collection<AttributeOrder> orderOf(final CMClass input) {
		final Iterable<? extends CMAttribute> source = from(input.getActiveAttributes()) //
				.filter(classOrder(not(equalTo(0))));
		final Iterable<? extends CMAttribute> sorted = from(BY_CLASS_ORDER) //
				.immutableSortedCopy(source);
		return from(sorted) //
				.transform(toAttributeOrder()) //
				.toList();

	}

	private Function<CMAttribute, AttributeOrder> toAttributeOrder() {
		return new Function<CMAttribute, AttributeOrder>() {

			@Override
			public AttributeOrder apply(final CMAttribute input) {
				return newAttributeOrder() //
						.withAttribute(input.getName()) //
						.withDirection(input.getClassOrder() > 0 ? ASCENDING : DESCENDING) //
						.build();
			}

		};
	}

}
