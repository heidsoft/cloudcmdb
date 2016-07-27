package org.cmdbuild.services.template.engine;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;
import org.cmdbuild.common.template.engine.Engine;
import org.cmdbuild.cql.facade.CQLAnalyzer.Callback;
import org.cmdbuild.cql.facade.CQLFacade;
import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.AnyAttribute;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.collect.Lists;

public class CqlEngine implements Engine {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<CqlEngine> {

		private CMDataView dataView;

		private Builder() {
			// use factory method
		}

		@Override
		public CqlEngine build() {
			validate();
			return new CqlEngine(this);
		}

		private void validate() {
			Validate.notNull(dataView, "missing '%s'", CMDataView.class);
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final Map<String, Object> EMPTY_CONTEXT = Collections.emptyMap();

	private static final class QueryCallback implements Callback {

		private final CMDataView dataView;

		public QueryCallback(final CMDataView dataView) {
			this.dataView = dataView;
		}

		private CMClass source;
		private Iterable<QueryAliasAttribute> attributes;
		private boolean distinct;
		private final Collection<Triple<CMClass, Alias, Over>> leftJoins = Lists.newArrayList();
		private final Collection<Triple<CMClass, Alias, Over>> joins = Lists.newArrayList();
		private final Collection<WhereClause> whereClauses = Lists.newArrayList();

		@Override
		public void from(final CMClass source) {
			this.source = source;
		}

		@Override
		public void attributes(final Iterable<QueryAliasAttribute> attributes) {
			this.attributes = attributes;
		}

		@Override
		public void distinct() {
			distinct = true;
		}

		@Override
		public void leftJoin(final CMClass target, final Alias alias, final Over over) {
			leftJoins.add(Triple.of(target, alias, over));
		}

		@Override
		public void join(final CMClass target, final Alias alias, final Over over) {
			joins.add(Triple.of(target, alias, over));
		}

		@Override
		public void where(final WhereClause clause) {
			whereClauses.add(clause);
		}

		public CMQueryResult execute() {
			final QuerySpecsBuilder querySpecsBuilder = dataView.select(anyAttribute(source)) //
					.from(source);
			if (distinct) {
				querySpecsBuilder.distinct();
			}
			for (final Triple<CMClass, Alias, Over> leftJoin : leftJoins) {
				querySpecsBuilder.leftJoin(leftJoin.getLeft(), leftJoin.getMiddle(), leftJoin.getRight());
			}
			for (final Triple<CMClass, Alias, Over> join : joins) {
				querySpecsBuilder.join(join.getLeft(), join.getMiddle(), join.getRight());
			}
			for (final WhereClause whereClause : whereClauses) {
				querySpecsBuilder.where(whereClause);
			}
			return querySpecsBuilder.run();
		}

	}

	private final CMDataView dataView;

	private CqlEngine(final Builder builder) {
		this.dataView = builder.dataView;
	}

	@Override
	public Object eval(final String expression) {
		final QueryCallback callback = new QueryCallback(dataView);
		CQLFacade.compileAndAnalyze(expression, EMPTY_CONTEXT, callback);
		final CMQueryResult result = callback.execute();
		final CMQueryRow row = result.getOnlyRow();
		final QueryAliasAttribute attribute = getOnlyElement(callback.attributes);
		if (attribute instanceof AnyAttribute) {
			throw new IllegalArgumentException();
		}
		final CMCard card = row.getCard(callback.source);
		final Object value;
		if (Const.ID_ATTRIBUTE.equals(attribute.getName())) {
			value = card.getId();
		} else {
			value = card.get(attribute.getName());
		}
		return value;
	}

}
