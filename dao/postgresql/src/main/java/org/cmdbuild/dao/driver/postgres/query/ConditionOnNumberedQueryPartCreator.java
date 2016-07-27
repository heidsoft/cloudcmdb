package org.cmdbuild.dao.driver.postgres.query;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForSystemAttribute;
import static org.cmdbuild.dao.driver.postgres.quote.AliasQuoter.quote;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.dao.query.clause.where.EmptyWhereClause.emptyWhereClause;

import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.ForwardingOperatorAndValueVisitor;
import org.cmdbuild.dao.query.clause.where.ForwardingWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.NullOperatorAndValueVisitor;
import org.cmdbuild.dao.query.clause.where.NullWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.OperatorAndValueVisitor;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClauseVisitor;

public class ConditionOnNumberedQueryPartCreator extends PartCreator {

	public ConditionOnNumberedQueryPartCreator(final QuerySpecs querySpecs, final StringBuilder main) {
		final WhereClause whereClause = defaultIfNull(querySpecs.getConditionOnNumberedQuery(), emptyWhereClause());
		whereClause.accept(new ForwardingWhereClauseVisitor() {

			private final WhereClauseVisitor delegate = NullWhereClauseVisitor.getInstance();

			@Override
			protected WhereClauseVisitor delegate() {
				return delegate;
			}

			@Override
			public void visit(final SimpleWhereClause whereClause) {
				whereClause.getOperator().accept(new ForwardingOperatorAndValueVisitor() {

					private final OperatorAndValueVisitor delegate = NullOperatorAndValueVisitor.getInstance();

					@Override
					protected OperatorAndValueVisitor delegate() {
						return delegate;
					}

					@Override
					public void visit(final EqualsOperatorAndValue operatorAndValue) {
						final QueryAliasAttribute attribute = whereClause.getAttribute();
						final String quotedName = quote(name(nameForSystemAttribute(attribute.getEntryTypeAlias(), Id)));
						final String actual = main.toString();
						main.setLength(0);
						sb.append(format("SELECT * FROM (%s) AS numbered WHERE %s = %s", //
								actual, //
								quotedName, //
								operatorAndValue.getValue()));
					}

				});
			}

		});
	}

}