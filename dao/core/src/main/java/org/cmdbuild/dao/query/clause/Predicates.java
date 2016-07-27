package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.base.Predicate;

public class Predicates {

	private static class WithAlias implements Predicate<QueryAliasAttribute> {

		private final Alias alias;

		public WithAlias(final Alias alias) {
			this.alias = alias;
		}

		@Override
		public boolean apply(final QueryAliasAttribute input) {
			return input.getEntryTypeAlias().equals(alias);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof WithAlias)) {
				return false;
			}
			final WithAlias other = WithAlias.class.cast(obj);
			return (this.alias.equals(other.alias));
		}

		@Override
		public int hashCode() {
			return alias.hashCode();
		}

		@Override
		public String toString() {
			return "WithAlias(" + alias + ")";
		}

	}

	public static Predicate<QueryAliasAttribute> withAlias(final Alias alias) {
		return new WithAlias(alias);
	}

	private Predicates() {
		// prevents instantiation
	}

}
