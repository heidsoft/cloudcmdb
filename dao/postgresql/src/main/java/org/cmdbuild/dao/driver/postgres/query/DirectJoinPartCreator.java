package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Status;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.driver.postgres.quote.AliasQuoter.quote;
import static org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter.quote;

import java.util.Iterator;

import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.HistoricEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;

import com.google.common.base.Function;

public class DirectJoinPartCreator extends PartCreator {

	private static class DirectJoinClauseToString implements Function<DirectJoinClause, String> {

		private static final String JOIN = "JOIN";
		private static final String LEFT_JOIN = "LEFT " + JOIN;

		private static final String FORMAT = "%s %s AS %s ON %s = %s";

		@Override
		public String apply(final DirectJoinClause input) {
			final QueryAliasAttribute sourceAttribute = input.getSourceAttribute();
			final QueryAliasAttribute targetAttribute = input.getTargetAttribute();
			String output = format(FORMAT, //
					input.isLeft() ? LEFT_JOIN : JOIN, //
					quote(input.getTargetClass()),//
					quote(input.getTargetClassAlias()), //
					quoteAttribute(targetAttribute.getEntryTypeAlias(), targetAttribute.getName()), //
					quoteAttribute(sourceAttribute.getEntryTypeAlias(), sourceAttribute.getName()) //
			);
			if (input.getTargetClass().holdsHistory() && !(input.getTargetClass() instanceof HistoricEntryType)) {
				output = format("%s AND %s = '%s'", //
						output, //
						quoteAttribute(targetAttribute.getEntryTypeAlias(), Status), //
						CardStatus.ACTIVE.value() //
				);

			}
			return output;
		}

	}

	private static DirectJoinClauseToString TO_STRING = new DirectJoinClauseToString();

	private static final String SEPARATOR = "\n";

	public DirectJoinPartCreator(final QuerySpecs querySpecs) {
		super();
		sb.append(join(directJoinClausesAsStrings(querySpecs), SEPARATOR));
	}

	private Iterator<String> directJoinClausesAsStrings(final QuerySpecs querySpecs) {
		return from(querySpecs.getDirectJoins()) //
				.transform(TO_STRING) //
				.toSet() // used here to avoid duplicate clauses
				.iterator();
	}

}
