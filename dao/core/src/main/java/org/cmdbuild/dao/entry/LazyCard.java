package org.cmdbuild.dao.entry;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Suppliers.synchronizedSupplier;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Supplier;

public class LazyCard extends ForwardingCard {

	private final CMCard delegate;
	private final CMDataView dataView;

	public LazyCard(final CMCard delegate, final CMDataView dataView) {
		this.delegate = delegate;
		this.dataView = dataView;
	}

	@Override
	protected CMCard delegate() {
		return synchronizedSupplier(memoize(card())).get();
	}

	private Supplier<CMCard> card() {
		return new Supplier<CMCard>() {

			@Override
			public CMCard get() {
				final CMClass target = delegate.getType();
				return dataView.select(anyAttribute(target)) //
						.from(target) //
						.where(condition(attribute(target, ID_ATTRIBUTE), eq(delegate.getId()))) //
						.limit(1) //
						.skipDefaultOrdering() //
						.run() //
						.getOnlyRow() //
						.getCard(target);
			}

		};
	}

}
