package org.cmdbuild.dao.query.clause.where;

import static java.util.Arrays.asList;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;

import com.google.common.collect.Lists;

public class WhereClauses {

	public static WhereClause alwaysFalse() {
		return FalseWhereClause.falseWhereClause();
	}

	public static WhereClause alwaysTrue() {
		return TrueWhereClause.trueWhereClause();
	}

	public static WhereClause and(final WhereClause first, final WhereClause second, final WhereClause... others) {
		final List<WhereClause> clauses = Lists.newArrayList();
		clauses.add(first);
		clauses.add(second);
		clauses.addAll(asList(others));
		return and(clauses);
	}

	public static WhereClause and(final Iterable<? extends WhereClause> whereClauses) {
		return AndWhereClause.and(whereClauses);
	}

	public static WhereClause condition(final QueryAliasAttribute attribute, final OperatorAndValue operator) {
		return SimpleWhereClause.condition(attribute, operator);
	}

	public static WhereClause empty() {
		return EmptyWhereClause.emptyWhereClause();
	}

	public static FunctionWhereClause function(final QueryAliasAttribute attribute, final String name,
			final Long userId, final Long roleId, final CMEntryType entryType) {
		return new FunctionWhereClause(attribute, name, userId, roleId, entryType);
	}

	public static WhereClause not(final WhereClause whereClause) {
		return NotWhereClause.not(whereClause);
	}

	public static WhereClause or(final WhereClause first, final WhereClause second, final WhereClause... others) {
		final List<WhereClause> clauses = Lists.newArrayList();
		clauses.add(first);
		clauses.add(second);
		clauses.addAll(asList(others));
		return or(clauses);
	}

	public static WhereClause or(final Iterable<? extends WhereClause> whereClauses) {
		return OrWhereClause.or(whereClauses);
	}

	private WhereClauses() {
		// prevents instantiation
	}

}
