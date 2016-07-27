package org.cmdbuild.dao.query.clause;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.base.Function;

public class Functions {

	public static final Function<QueryAliasAttribute, String> name() {
		return new Function<QueryAliasAttribute, String>() {

			@Override
			public String apply(final QueryAliasAttribute input) {
				return input.getName();
			}

		};
	}

	private static class ToQueryAliasAttributeWithAlias implements Function<CMAttribute, QueryAliasAttribute> {

		private final Alias alias;

		public ToQueryAliasAttributeWithAlias(final Alias alias) {
			this.alias = alias;
		}

		@Override
		public QueryAliasAttribute apply(final CMAttribute input) {
			return attribute(alias, input);
		}

	}

	public static Function<CMAttribute, QueryAliasAttribute> queryAliasAttribute(final Alias alias) {
		return new ToQueryAliasAttributeWithAlias(alias);
	}

	private static class ToQueryAliasAttributeWithEntryType implements Function<CMAttribute, QueryAliasAttribute> {

		private final CMEntryType entryType;

		public ToQueryAliasAttributeWithEntryType(final CMEntryType entryType) {
			this.entryType = entryType;
		}

		@Override
		public QueryAliasAttribute apply(final CMAttribute input) {
			return attribute(entryType, input);
		}

	}

	public static Function<CMAttribute, QueryAliasAttribute> queryAliasAttribute(final CMEntryType entryType) {
		return new ToQueryAliasAttributeWithEntryType(entryType);
	}

	private Functions() {
		// prevents instantiation
	}

}
