package org.cmdbuild.service.rest.v2.cxf.serialization;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Ordering.from;
import static java.lang.Math.abs;
import static org.cmdbuild.dao.entrytype.Predicates.classOrder;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ASCENDING;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCENDING;
import static org.cmdbuild.service.rest.v2.model.Models.newAttributeOrder;
import static org.cmdbuild.service.rest.v2.model.Models.newClassWithFullDetails;

import java.util.Collection;
import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.service.rest.v2.model.ClassWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ClassWithFullDetails.AttributeOrder;

import com.google.common.base.Function;

public class ToFullClassDetail implements Function<CMClass, ClassWithFullDetails> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToFullClassDetail> {

		private Builder() {
			// use static method
		}

		@Override
		public ToFullClassDetail build() {
			return new ToFullClassDetail(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private ToFullClassDetail(final Builder builder) {
		// nothing to do
	}

	@Override
	public ClassWithFullDetails apply(final CMClass input) {
		final CMClass parent = input.getParent();
		return newClassWithFullDetails() //
				.withId(input.getName()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.thatIsPrototype(input.isSuperclass()) //
				.withDescriptionAttributeName(input.getDescriptionAttributeName()) //
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
