package org.cmdbuild.logic.data.access;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.entrytype.Functions.attribute;
import static org.cmdbuild.dao.query.ExternalReferenceAliasHandler.EXTERNAL_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.Functions.queryAliasAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.as;
import static org.cmdbuild.dao.query.clause.alias.Aliases.canonical;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARDS_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DESTINATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_DIRECTION;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ANY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_NOONE;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ONEOF;

import java.util.List;
import java.util.Map;

import org.cmdbuild.cql.facade.CQLAnalyzer.NullCallback;
import org.cmdbuild.cql.facade.CQLFacade;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.ExternalReferenceAliasHandler;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.Over;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonAttributeFilterBuilder;
import org.cmdbuild.logic.mapping.json.JsonFullTextQueryBuilder;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;
import org.cmdbuild.logic.validation.json.JsonFilterValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

public class QuerySpecsBuilderFiller {

	private static <E> FluentIterable<E> _from(final Iterable<E> iterable) {
		return FluentIterable.from(iterable);
	}

	private final CMDataView dataView;
	private final QueryOptions queryOptions;
	private CMEntryType entryType;

	public QuerySpecsBuilderFiller(final CMDataView dataView, final QueryOptions queryOptions,
			final CMEntryType entryType) {
		this.dataView = dataView;
		this.queryOptions = queryOptions;
		this.entryType = entryType;
	}

	public Alias getAlias() {
		return canonical(entryType);
	}

	public QuerySpecsBuilder create() {
		final Iterable<QueryAliasAttribute> attributeSubsetForSelect = _from(queryOptions.getAttributes()) //
				.transform(attribute(entryType)) //
				.filter(CMAttribute.class) //
				.transform(queryAliasAttribute(entryType));
		final Object[] attributesArray;
		if (isEmpty(attributeSubsetForSelect)) {
			attributesArray = new Object[] { anyAttribute(entryType) };
		} else {
			attributesArray = toArray(attributeSubsetForSelect, Object.class);
		}
		final QuerySpecsBuilder querySpecsBuilder = dataView.select(attributesArray) //
				.from(entryType, as(getAlias()));
		try {
			fillQuerySpecsBuilderWithFilterOptions(querySpecsBuilder);
		} catch (final JSONException ex) {
			Log.CMDBUILD.error("Bad filter. The filter is {} ", queryOptions.getFilter().toString());
		}
		querySpecsBuilder //
				.limit(queryOptions.getLimit()) //
				.offset(queryOptions.getOffset());
		addSortingOptions(querySpecsBuilder);
		return querySpecsBuilder;
	}

	private void addSortingOptions(final QuerySpecsBuilder querySpecsBuilder) {
		final SorterMapper sorterMapper = new JsonSorterMapper(entryType, queryOptions.getSorters(), getAlias());
		final List<OrderByClause> clauses = sorterMapper.deserialize();
		if (clauses.isEmpty()) {
			if (entryType.getAttribute(DESCRIPTION_ATTRIBUTE) != null) {
				querySpecsBuilder.orderBy(attribute(getAlias(), DESCRIPTION_ATTRIBUTE), Direction.ASC);
			}
		} else {
			for (final OrderByClause clause : clauses) {
				final Object attributeAlias = getAttributeAliasFromOrderClause(clause);
				querySpecsBuilder.orderBy(attributeAlias, clause.getDirection());
			}
		}
	}

	/**
	 * Sorting by lookup, reference and foreign key attributes must add column
	 * the description of the relative card/lookup
	 * 
	 * @param entryType
	 * @param clause
	 * @return the alias to use to sort
	 */
	private Object getAttributeAliasFromOrderClause(final OrderByClause clause) {
		final Optional<String> pattern = new ForwardingAttributeTypeVisitor() {

			private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();
			private final QueryAliasAttribute queryAttribute = clause.getAttribute();
			private final String entryTypeAlias = queryAttribute.getEntryTypeAlias().toString();
			private final String attributeName = queryAttribute.getName();
			private final CMAttribute cmAttribute = entryType.getAttribute(attributeName);
			private final CMAttributeType<?> cmAttributeType = (cmAttribute == null) ? null : cmAttribute.getType();

			private Optional<String> pattern;

			@Override
			protected CMAttributeTypeVisitor delegate() {
				return DELEGATE;
			}

			public Optional<String> pattern() {
				pattern = absent();
				if (cmAttributeType != null) {
					cmAttributeType.accept(this);
				}
				return pattern;
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				pattern = of(value());
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				pattern = of(value());
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				final Optional<String> referencedClassName = getReferencedClassName(cmAttributeType);
				/*
				 * if no referenced class name is found return only the
				 * attribute name
				 */
				if (referencedClassName.isPresent() && isNotEmpty(referencedClassName.get())) {
					pattern = of(value());
				}
			}

			private String value() {
				return new ExternalReferenceAliasHandler(entryTypeAlias, cmAttribute).forQuery();
			}

		}.pattern();
		final Object attributeAlias;
		if (pattern.isPresent()) {
			attributeAlias = QueryAliasAttribute.attribute(name(pattern.get()), EXTERNAL_ATTRIBUTE);
		} else {
			attributeAlias = clause.getAttribute();
		}
		return attributeAlias;
	}

	private Optional<String> getReferencedClassName(final CMAttributeType<?> cmAttributeType) {
		final String domainName = ((ReferenceAttributeType) cmAttributeType).getDomainName();
		final CMDomain domain = dataView.findDomain(domainName);
		final Optional<String> output;
		if (domain == null) {
			output = absent();
		} else if (CARDINALITY_1N.value().equals(domain.getCardinality())) {
			output = of(domain.getClass1().getName());
		} else if (CARDINALITY_N1.value().equals(domain.getCardinality())) {
			output = of(domain.getClass2().getName());
		} else {
			output = absent();
		}
		return output;
	}

	/**
	 * TODO: split into different private methods
	 */
	private void fillQuerySpecsBuilderWithFilterOptions(final QuerySpecsBuilder querySpecsBuilder)
			throws JSONException {
		final List<WhereClause> whereClauses = newArrayList();
		final JSONObject filterObject = queryOptions.getFilter();
		new JsonFilterValidator(queryOptions.getFilter()).validate();

		// CQL filter
		if (filterObject.has(CQL_KEY)) {
			Log.CMDBUILD.info("Filter is a CQL filter");
			final String cql = filterObject.getString(CQL_KEY);
			final Map<String, Object> context = queryOptions.getParameters();
			CQLFacade.compileAndAnalyze(cql, context, new NullCallback() {

				@Override
				public void from(final CMClass source) {
					entryType = source;
					querySpecsBuilder.select(anyAttribute(source)) //
							.from(source);
				}

				@Override
				public void distinct() {
					querySpecsBuilder.distinct();
				}

				@Override
				public void leftJoin(final CMClass target, final Alias alias, final Over over) {
					querySpecsBuilder.leftJoin(target, alias, over);
				}

				@Override
				public void join(final CMClass target, final Alias alias, final Over over) {
					querySpecsBuilder.join(target, alias, over);
				}

				@Override
				public void where(final WhereClause clause) {
					whereClauses.add(clause);
					querySpecsBuilder.where(clause);
				}

			});
		}

		// full text query on attributes of the source class
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			whereClauses.add(JsonFullTextQueryBuilder.newInstance() //
					.withFullTextQuery(filterObject.getString(FULL_TEXT_QUERY_KEY)) //
					.withEntryType(entryType) //
					.build());
		}

		if (filterObject.has(CQL_KEY)) {
			querySpecsBuilder.where(isEmpty(whereClauses) ? trueWhereClause() : and(whereClauses));
			return;
		}

		// filter on attributes of the source class
		if (filterObject.has(ATTRIBUTE_KEY)) {
			whereClauses.add(JsonAttributeFilterBuilder.newInstance() //
					.withFilterObject(filterObject.getJSONObject(ATTRIBUTE_KEY)) //
					.withEntryType(entryType) //
					.withDataView(dataView) //
					.build());
		}

		// filter on relations
		if (filterObject.has(RELATION_KEY)) {
			querySpecsBuilder.distinct();
			final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
			for (int i = 0; i < conditions.length(); i++) {

				final JSONObject condition = conditions.getJSONObject(i);
				final String domainName = condition.getString(RELATION_DOMAIN_KEY);
				final String sourceString = condition.getString(RELATION_DOMAIN_DIRECTION);
				final CMDomain domain = dataView.findDomain(domainName);
				final String destinationName = condition.getString(RELATION_DESTINATION_KEY);
				final CMClass destinationClass = dataView.findClass(destinationName);
				final boolean left = condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE);
				final Alias destinationAlias = name(format("DST-%s-%s", destinationName, randomNumeric(10)));
				final Alias domainAlias = name(format("DOM-%s-%s", domainName, randomNumeric(10)));

				if (left) {
					querySpecsBuilder.leftJoin(destinationClass, destinationAlias, over(domain, domainAlias),
							getSourceFrom(sourceString));
				} else {
					querySpecsBuilder.join(destinationClass, destinationAlias, over(domain, domainAlias),
							getSourceFrom(sourceString));
				}

				final String conditionType = condition.getString(RELATION_TYPE_KEY);

				if (conditionType.equals(RELATION_TYPE_ONEOF)) {
					final JSONArray cards = condition.getJSONArray(RELATION_CARDS_KEY);
					final List<Long> oneOfIds = newArrayList();

					for (int j = 0; j < cards.length(); j++) {
						final JSONObject card = cards.getJSONObject(j);
						oneOfIds.add(card.getLong(RELATION_CARD_ID_KEY));
					}
					whereClauses.add( //
							condition( //
									attribute(destinationAlias, Id.getDBName()), //
									in(oneOfIds.toArray())));

				} else if (conditionType.equals(RELATION_TYPE_NOONE)) {
					whereClauses.add( //
							condition( //
									attribute(destinationAlias, Id.getDBName()), //
									isNull()));
				} else if (conditionType.equals(RELATION_TYPE_ANY)) {
					/**
					 * Should be empty. WhereClauses not added because I can
					 * detect if a card is in relation with ANY card, using only
					 * the JOIN clause
					 */
				}

			}
		}
		if (!whereClauses.isEmpty()) {
			querySpecsBuilder.where(and(whereClauses));
		} else {
			querySpecsBuilder.where(trueWhereClause());
		}
	}

	private Source getSourceFrom(final String source) {
		return Source._1.name().equals(source) ? Source._1 : Source._2;
	}

}
