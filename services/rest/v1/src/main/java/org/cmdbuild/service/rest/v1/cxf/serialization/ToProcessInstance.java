package org.cmdbuild.service.rest.v1.cxf.serialization;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Maps.filterKeys;
import static com.google.common.collect.Maps.transformEntries;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.utils.guava.Functions.toKey;
import static org.cmdbuild.common.utils.guava.Functions.toValue;
import static org.cmdbuild.service.rest.v1.model.Models.newProcessInstance;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.service.rest.v1.model.ProcessInstance;
import org.cmdbuild.workflow.LookupHelper;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps.EntryTransformer;

public class ToProcessInstance implements Function<UserProcessInstance, ProcessInstance> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToProcessInstance> {

		private static final Iterable<String> NO_ATTRIBUTES = emptyList();

		private CMClass type;
		private LookupHelper lookupHelper;
		private Iterable<String> attributes;

		private Builder() {
			// use static method
		}

		@Override
		public ToProcessInstance build() {
			validate();
			return new ToProcessInstance(this);
		}

		private void validate() {
			Validate.notNull(type, "missing '%s'", CMClass.class);
			attributes = defaultIfNull(attributes, NO_ATTRIBUTES);
		}

		public Builder withType(final CMClass type) {
			this.type = type;
			return this;
		}

		public Builder withLookupHelper(final LookupHelper lookupHelper) {
			this.lookupHelper = lookupHelper;
			return this;
		}

		public Builder withAttributes(final Iterable<String> attributes) {
			this.attributes = attributes;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final Function<Entry<? extends String, ? extends Object>, String> KEY = toKey();
	private static final Function<Entry<? extends String, ? extends Object>, Object> VALUE = toValue();

	private static final ToProcessStatus TO_PROCESS_STATUS = new ToProcessStatus();

	private static final Predicate<String> ALL_ATTRIBUTES_FILTER = alwaysTrue();

	private final CMClass type;
	private final LookupHelper lookupHelper;
	private final Iterable<String> attributes;

	private ToProcessInstance(final Builder builder) {
		this.type = builder.type;
		this.lookupHelper = builder.lookupHelper;
		this.attributes = builder.attributes;
	}

	@Override
	public ProcessInstance apply(final UserProcessInstance input) {
		final Map<String, Object> values = transformValues(uniqueIndex(input.getAllValues(), KEY), VALUE);
		final Predicate<String> filter = isEmpty(attributes) ? ALL_ATTRIBUTES_FILTER : new Predicate<String>() {

			@Override
			public boolean apply(final String input) {
				return contains(attributes, input);
			}

		};
		final EntryTransformer<String, Object, Object> transformer = new EntryTransformer<String, Object, Object>() {

			@Override
			public Object transformEntry(final String key, final Object value) {
				final CMAttribute attribute = type.getAttribute(key);
				Object output;
				if (attribute == null) {
					output = value;
				} else {
					final CMAttributeType<?> attributeType = attribute.getType();
					output = DefaultConverter.newInstance() //
							.build() //
							.toClient() //
							.convert(attributeType, value);
				}
				return output;
			}

		};
		final WSProcessInstanceState state = input.getState();
		final Optional<Lookup> lookupForState = lookupHelper.lookupForState(state);
		return newProcessInstance() //
				.withType(input.getType().getName()) //
				.withId(input.getId()) //
				.withName(input.getProcessInstanceId()) //
				.withStatus(lookupForState.isPresent() ? TO_PROCESS_STATUS.apply(lookupForState.get()).getId() : null) //
				.withValues(transformEntries( //
						filterKeys(values, filter), //
						transformer)) //
				.build();
	}

}
