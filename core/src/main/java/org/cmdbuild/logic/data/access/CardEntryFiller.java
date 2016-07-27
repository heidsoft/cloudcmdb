package org.cmdbuild.logic.data.access;

import static com.google.common.collect.FluentIterable.from;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver.EntryFiller;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class CardEntryFiller extends EntryFiller<CMCard> {

	private static class StaticForwarder extends ForwardingCard {

		private final CMCard delegate;
		private final Map<String, Object> values;
		private final boolean includeSystemAttributes;

		public StaticForwarder(final CMCard delegate, final Map<String, Object> currentValues,
				final boolean includeSystemAttributes) {
			this.delegate = delegate;
			this.values = Maps.newHashMap(currentValues);
			this.includeSystemAttributes = includeSystemAttributes;
		}

		@Override
		protected CMCard delegate() {
			return delegate;
		}

		@Override
		public Iterable<Entry<String, Object>> getAllValues() {
			return values.entrySet();
		}

		@Override
		public Iterable<Entry<String, Object>> getValues() {
			return from(getAllValues()) //
					.filter(new Predicate<Map.Entry<String, Object>>() {

						@Override
						public boolean apply(final Entry<String, Object> input) {
							final String name = input.getKey();
							final CMAttribute attribute = getType().getAttribute(name);
							return (attribute != null) && (includeSystemAttributes || !attribute.isSystem());
						}

					});
		}

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<CardEntryFiller> {

		private boolean includeSystemAttributes;

		/**
		 * use factory method
		 */
		private Builder() {
		}

		@Override
		public CardEntryFiller build() {
			return new CardEntryFiller(this);
		}

		public Builder includeSystemAttributes(final boolean includeSystemAttributes) {
			this.includeSystemAttributes = includeSystemAttributes;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final boolean includeSystemAttributes;

	private CardEntryFiller(final Builder builder) {
		this.includeSystemAttributes = builder.includeSystemAttributes;
	}

	@Override
	public CMCard getOutput() {
		return new StaticForwarder(input, values, includeSystemAttributes);
	}

}
