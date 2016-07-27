package org.cmdbuild.logic.commands;

import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.where.ForwardingWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.NullWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClauseVisitor;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;
import org.cmdbuild.model.data.Card;

import com.google.common.base.Function;

public class GetRelationList extends AbstractGetRelation {

	private boolean emptyCardForWrongId;

	public GetRelationList(final CMDataView view) {
		super(view);
	}

	/**
	 * @param domainWithSource
	 *            The domain to list grouped by source
	 * @return The relations of this domain grouped by the id of the source card
	 */
	public Map<Object, List<RelationInfo>> list(final String sourceTypeName, final DomainWithSource domainWithSource) {
		final CMDomain domain = getQueryDomain(domainWithSource);
		final CMClass sourceType = view.findClass(sourceTypeName);
		final CMQueryResult relations = getRelationQuery(sourceType, domain).run();
		return fillMap(relations);
	}

	public GetRelationList emptyForWrongId() {
		emptyCardForWrongId = true;
		return this;
	}

	private Map<Object, List<RelationInfo>> fillMap(final CMQueryResult relationList) {
		final Map<Object, List<RelationInfo>> result = new HashMap<Object, List<RelationInfo>>();
		for (final CMQueryRow row : relationList) {
			final CMCard src = row.getCard(SRC_ALIAS);
			final CMCard dst = row.getCard(DST_ALIAS);
			final QueryRelation rel = row.getRelation(DOM_ALIAS);
			final RelationInfo relInfo = new RelationInfo(rel, src, dst);

			List<RelationInfo> relations;
			if (!result.containsKey(src.getId())) {
				relations = new ArrayList<RelationInfo>();
				result.put(src.getId(), relations);
			} else {
				relations = result.get(src.getId());
			}

			relations.add(relInfo);
		}

		return result;
	}

	public GetRelationListResponse exec(final Card src, final DomainWithSource domainWithSource,
			final QueryOptions queryOptions) {
		Validate.notNull(src);

		if (emptyCardForWrongId) {
			final Long cardId = src.getId();
			if (cardId == null || cardId <= 0) {
				return new GetRelationListResponse();
			}
		}

		final CMClass sourceType = view.findClass(src.getClassName());
		final String domainSource = (domainWithSource != null) ? domainWithSource.querySource : null;

		if (sourceType.isSimple()) {
			return createRelationListResponse(CMQueryResult.EMPTY, domainSource);
		} else {
			final SorterMapper sorterMapper = new JsonSorterMapper(sourceType, queryOptions.getSorters());
			final List<OrderByClause> orderByClauses = sorterMapper.deserialize();
			final FilterMapper filterMapper = JsonFilterMapper.newInstance() //
					.withDataView(view) //
					.withEntryType(sourceType) //
					.withEntryTypeAlias(SRC_ALIAS) //
					.withFilterObject(queryOptions.getFilter()) //
					.build();
			final Iterable<WhereClause> whereClauses = filterMapper.whereClauses();
			final WhereClause filtersOnRelations = isEmpty(whereClauses) ? trueWhereClause() : and(whereClauses);
			final CMDomain domain = getQueryDomain(domainWithSource);
			final QuerySpecsBuilder querySpecsBuilder = getRelationQuerySpecsBuilder(src, domain, filtersOnRelations) //
					.limit(queryOptions.getLimit()) //
					.offset(queryOptions.getOffset());
			addOrderByClauses(querySpecsBuilder, orderByClauses);

			final CMQueryResult relationList = querySpecsBuilder.run();
			return createRelationListResponse(relationList, domainSource);
		}
	}

	public GetRelationListResponse exec(final CMDomain domain, final QueryOptions queryOptions) {
		final CMClass sourceType = domain.getClass1();
		final SorterMapper sorterMapper = new JsonSorterMapper(sourceType, queryOptions.getSorters());
		final List<OrderByClause> orderByClauses = sorterMapper.deserialize();
		final FilterMapper filterMapper = JsonFilterMapper.newInstance() //
				.withDataView(view) //
				.withEntryType(sourceType) //
				.withEntryTypeAlias(SRC_ALIAS) //
				.withFilterObject(queryOptions.getFilter()) //
				/*
				 * FIXME temporary hack
				 * 
				 * we must be able to refer to source/destination id within
				 * filter. basically, the filter is referred to the subject of
				 * the query. despite that when referring to relations the
				 * subject of the query is the domain, the from clause is always
				 * the source class.
				 */
				.withFunction(new Function<WhereClause, WhereClause>() {

					@Override
					public WhereClause apply(final WhereClause input) {
						return new ForwardingWhereClauseVisitor() {

							private final WhereClauseVisitor delegate = NullWhereClauseVisitor.getInstance();

							private WhereClause output;

							public WhereClause apply() {
								input.accept(this);
								return output;
							}

							@Override
							protected WhereClauseVisitor delegate() {
								return delegate;
							}

							@Override
							public void visit(final SimpleWhereClause whereClause) {
								final QueryAliasAttribute attribute = whereClause.getAttribute();
								final String name = attribute.getName();
								final QueryAliasAttribute _attribute;
								if (ID.equals(name)) {
									_attribute = attribute(DOM_ALIAS, name);
								} else if (IDOBJ1.equals(name)) {
									_attribute = attribute(DOM_ALIAS, name);
								} else if (IDOBJ2.equals(name)) {
									_attribute = attribute(DOM_ALIAS, name);
								} else if ("_Src".equals(name)) {
									_attribute = attribute(DOM_ALIAS, name);
								} else {
									_attribute = attribute;
								}
								output = condition(_attribute, whereClause.getOperator());
							}

						}.apply();
					}

				}).build();
		final Iterable<WhereClause> whereClauses = filterMapper.whereClauses();
		final WhereClause filtersOnRelations = isEmpty(whereClauses) ? trueWhereClause() : and(whereClauses);
		final QuerySpecsBuilder querySpecsBuilder = getRelationQuerySpecsBuilder( //
				Card.newInstance(sourceType) //
						.build(), //
				domain, //
				filtersOnRelations);
		querySpecsBuilder.limit(queryOptions.getLimit()) //
				.offset(queryOptions.getOffset());
		addOrderByClauses(querySpecsBuilder, orderByClauses);

		final CMQueryResult relationList = querySpecsBuilder.run();
		return createRelationListResponse(relationList, null);
	}

	private CMDomain getQueryDomain(final DomainWithSource domainWithSource) {
		final CMDomain dom;
		if (domainWithSource != null) {
			dom = view.findDomain(domainWithSource.domainId);
			Validate.notNull(dom);
		} else {
			dom = anyDomain();
		}
		return dom;
	}

	private void addOrderByClauses(final QuerySpecsBuilder querySpecsBuilder, final List<OrderByClause> orderByClauses) {
		for (final OrderByClause clause : orderByClauses) {
			querySpecsBuilder.orderBy(clause.getAttribute(), clause.getDirection());
		}
	}

	// FIXME Implement domain direction in queries and remove the domainSource
	// hack!
	private GetRelationListResponse createRelationListResponse(final CMQueryResult result, final String domainSource) {
		final GetRelationListResponse out = new GetRelationListResponse();
		for (final CMQueryRow row : result) {
			final CMCard src = row.getCard(SRC_ALIAS);
			final CMCard dst = row.getCard(DST_ALIAS);
			if (dst != null) {
				final QueryRelation rel = row.getRelation(DOM_ALIAS);
				if (domainSource != null && !domainSource.equals(rel.getQueryDomain().getQuerySource())) {
					continue;
				}
				// TODO: check here if the dst match the filter....
				out.addRelation(rel, src, dst);
			}
		}
		out.setTotalNumberOfRelations(result.totalSize());
		return out;
	}

	public static class GetRelationListResponse extends GetRelationResponse implements Iterable<DomainInfo> {
		private final List<DomainInfo> domainInfos;
		private int totalNumberOfRelations;

		private GetRelationListResponse() {
			domainInfos = new ArrayList<DomainInfo>();
		}

		@Override
		protected void doAddRelation(final RelationInfo relationInfo) {
			getOrCreateDomainInfo(relationInfo.getQueryDomain()).addRelationInfo(relationInfo);
		}

		private DomainInfo getOrCreateDomainInfo(final QueryDomain qd) {
			for (final DomainInfo di : domainInfos) {
				if (di.getQueryDomain().equals(qd)) {
					return di;
				}
			}
			return addDomainInfo(qd);
		}

		private DomainInfo addDomainInfo(final QueryDomain qd) {
			final DomainInfo di = new DomainInfo(qd);
			domainInfos.add(di);
			return di;
		}

		private void setTotalNumberOfRelations(final int totalNumberOfRelations) {
			this.totalNumberOfRelations = totalNumberOfRelations;
		}

		@Override
		public Iterator<DomainInfo> iterator() {
			return domainInfos.iterator();
		}

		public int getTotalNumberOfRelations() {
			return totalNumberOfRelations;
		}
	}

	public static class DomainInfo implements Iterable<RelationInfo> {
		private final QueryDomain querydomain;
		private final List<RelationInfo> relations;

		private DomainInfo(final QueryDomain queryDomain) {
			this.querydomain = queryDomain;
			this.relations = new ArrayList<RelationInfo>();
		}

		public QueryDomain getQueryDomain() {
			return querydomain;
		}

		private void addRelationInfo(final RelationInfo ri) {
			relations.add(ri);
		}

		public String getDescription() {
			return querydomain.getDescription();
		}

		@Override
		public Iterator<RelationInfo> iterator() {
			return relations.iterator();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((querydomain == null) ? 0 : querydomain.hashCode());
			result = prime * result + ((relations == null) ? 0 : relations.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final DomainInfo other = (DomainInfo) obj;
			if (!this.querydomain.getDomain().getId().equals(other.getQueryDomain().getDomain().getId())) {
				return false;
			}
			return true;
		}

	}

	public static class DomainWithSource {

		public static DomainWithSource create(final CMDomain domain) {
			return new DomainWithSource(domain.getId(), null);
		}

		public static DomainWithSource create(final Long domainId, final String querySource) {
			final DomainWithSource dom;
			if (domainId != null && querySource != null) {
				dom = new DomainWithSource(domainId, querySource);
			} else {
				dom = null;
			}
			return dom;
		}

		public final Long domainId;
		public final String querySource;

		private DomainWithSource(final Long domainId, final String querySource) {
			this.domainId = domainId;
			this.querySource = querySource;
		}

		@Override
		public String toString() {
			return String.format("%s.%s", domainId, querySource);
		}

	}

}
