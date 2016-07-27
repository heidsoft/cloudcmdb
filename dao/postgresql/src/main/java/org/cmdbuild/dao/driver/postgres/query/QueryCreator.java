package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import java.util.List;

import org.cmdbuild.dao.query.QuerySpecs;

public class QueryCreator {

	private static final String SPACE = " ";

	private static final String PARTS_SEPARATOR = SPACE + LINE_SEPARATOR;

	private final StringBuilder sb;
	private final QuerySpecs querySpecs;
	private final List<Object> params;

	private SelectAttributesExpressions selectAttributesExpressions;
	private ColumnMapper columnMapper;

	public QueryCreator(final QuerySpecs querySpecs) {
		this.sb = new StringBuilder();
		this.querySpecs = querySpecs;
		this.params = newArrayList();
		buildQuery();
	}

	private void buildQuery() {
		selectAttributesExpressions = new SelectAttributesExpressions();
		columnMapper = new ColumnMapper(querySpecs, selectAttributesExpressions);
		columnMapper.addAllAttributes(querySpecs.getAttributes());

		appendSelect();
		appendFrom();
		appendDirectJoin();
		appendJoin();
		appendWhere();
		appendNumberingAndOrder();
		appendLimitAndOffset();
		appendConditionOnNumberedQuery();
	}

	private void appendSelect() {
		appendPart(new SelectPartCreator(querySpecs, columnMapper, selectAttributesExpressions));
	}

	private void appendFrom() {
		appendPart(new FromPartCreator(querySpecs));
	}

	private void appendDirectJoin() {
		appendPart(new DirectJoinPartCreator(querySpecs));
	}

	private void appendJoin() {
		appendPart(new JoinCreator(querySpecs.getFromClause().getAlias(), querySpecs.getJoins(), columnMapper));
	}

	private void appendWhere() {
		appendPart(new WherePartCreator(querySpecs));
	}

	private void appendNumberingAndOrder() {
		appendPart(new NumberingAndOrderPartCreator(querySpecs, sb));
	}

	private void appendConditionOnNumberedQuery() {
		appendPart(new ConditionOnNumberedQueryPartCreator(querySpecs, sb));
	}

	private void appendLimitAndOffset() {
		appendPart(new LimitPartCreator(querySpecs));
		appendPart(new OffsetPartCreator(querySpecs));
	}

	private void appendPart(final PartCreator partCreator) {
		final String part = partCreator.getPart();
		if (isNotEmpty(part)) {
			sb.append(PARTS_SEPARATOR).append(part);
			params.addAll(partCreator.getParams());
		}
	}

	public String getQuery() {
		return sb.toString();
	}

	public Object[] getParams() {
		return params.toArray();
	}

	public ColumnMapper getColumnMapper() {
		return columnMapper;
	}

}
