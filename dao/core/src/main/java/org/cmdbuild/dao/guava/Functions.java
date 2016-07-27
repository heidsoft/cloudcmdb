package org.cmdbuild.dao.guava;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.base.Function;

public class Functions {

	public static Function<CMQueryRow, CMCard> toCard(final CMClass type) {
		return new Function<CMQueryRow, CMCard>() {

			@Override
			public CMCard apply(final CMQueryRow input) {
				return input.getCard(type);
			}

		};
	}

	public static Function<CMQueryRow, CMCard> toCard(final Alias alias) {
		return new Function<CMQueryRow, CMCard>() {

			@Override
			public CMCard apply(final CMQueryRow input) {
				return input.getCard(alias);
			}

		};
	}
	
	public static Function<CMQueryRow, CMRelation> toRelation(final CMDomain type) {
		return new Function<CMQueryRow, CMRelation>() {

			@Override
			public CMRelation apply(final CMQueryRow input) {
				return input.getRelation(type).getRelation();
			}

		};
	}

	public static Function<CMQueryRow, CMRelation> toRelation(final Alias alias) {
		return new Function<CMQueryRow, CMRelation>() {

			@Override
			public CMRelation apply(final CMQueryRow input) {
				return input.getRelation(alias).getRelation();
			}

		};
	}

	public static Function<CMQueryRow, CMValueSet> toValueSet(final Alias alias) {
		return new Function<CMQueryRow, CMValueSet>() {

			@Override
			public CMValueSet apply(final CMQueryRow input) {
				return input.getValueSet(alias);
			}

		};
	}

	public static <T> Function<CMCard, T> toAttribute(final String name, final Class<T> type) {
		return new Function<CMCard, T>() {

			@Override
			public T apply(final CMCard input) {
				return input.get(name, type);
			}

		};
	}

	private Functions() {
		// prevents instantiation
	}

}
