package org.cmdbuild.logic.commands;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.condition;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class GetRelationSingle extends AbstractGetRelation {

	public GetRelationSingle(final CMDataView dataView) {
		super(dataView);
	}

	public Optional<RelationInfo> exec(final CMDomain domain, final Long id) {
		final CMClass source = domain.getClass1();
		return from(getRelationQuery(source, domain) //
				.where(condition(attribute(DOM_ALIAS, ID), eq(id))) //
				.run()) //
						.transform(new Function<CMQueryRow, RelationInfo>() {

							@Override
							public RelationInfo apply(final CMQueryRow input) {
								final QueryRelation rel = input.getRelation(DOM_ALIAS);
								final CMCard src = input.getCard(SRC_ALIAS);
								final CMCard dst = input.getCard(DST_ALIAS);
								return new RelationInfo(rel, src, dst);
							}

						}) //
						.filter(new Predicate<RelationInfo>() {

							@Override
							public boolean apply(final RelationInfo input) {
								return input.getQueryDomain().getDirection();
							}

						}) //
						.first();
	}

}
