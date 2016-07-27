package org.cmdbuild.logic.data.access;

import static com.google.common.collect.FluentIterable.from;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver.EntryFiller;
import org.cmdbuild.workflow.user.ForwardingUserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class ProcessEntryFiller extends EntryFiller<UserProcessInstance> {

	private static class StaticForwarder extends ForwardingUserProcessInstance {

		private final UserProcessInstance delegate;
		private final Map<String, Object> values;

		public StaticForwarder(final UserProcessInstance delegate, final Map<String, Object> currentValues) {
			this.delegate = delegate;
			this.values = Maps.newHashMap(currentValues);
		}

		@Override
		protected UserProcessInstance delegate() {
			return delegate;
		}

		@Override
		public Object get(final String key) {
			return values.get(key);
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
							return !attribute.isSystem();
						}
					});
		}

	}

	@Override
	public UserProcessInstance getOutput() {
		return new StaticForwarder(input, values);
	}

}
