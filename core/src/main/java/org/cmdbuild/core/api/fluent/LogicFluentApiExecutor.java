package org.cmdbuild.core.api.fluent;

import static org.cmdbuild.logic.data.lookup.LookupLogic.UNUSED_LOOKUP_QUERY;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.ExistingCard;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.ForwardingFluentApiExecutor;
import org.cmdbuild.api.fluent.Lookup;
import org.cmdbuild.api.fluent.QueryAllLookup;
import org.cmdbuild.api.fluent.QuerySingleLookup;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class LogicFluentApiExecutor extends ForwardingFluentApiExecutor {

	private static final FluentApiExecutor UNSUPPORTED = UnsupportedProxyFactory.of(FluentApiExecutor.class).create();

	private final DataAccessLogic dataAccessLogic;
	private final LookupLogic lookupLogic;

	public LogicFluentApiExecutor(final DataAccessLogic dataAccessLogic, final LookupLogic lookupLogic) {
		this.dataAccessLogic = dataAccessLogic;
		this.lookupLogic = lookupLogic;
	}

	@Override
	protected FluentApiExecutor delegate() {
		return UNSUPPORTED;
	}

	@Override
	public void update(final ExistingCard card) {
		final org.cmdbuild.model.data.Card cardToSave = FLUENT_API_TO_MODEL_CARD.apply(card);
		dataAccessLogic.updateCard(cardToSave);
	}

	final static Function<ExistingCard, org.cmdbuild.model.data.Card> FLUENT_API_TO_MODEL_CARD = new Function<ExistingCard, org.cmdbuild.model.data.Card>() {

		@Override
		public org.cmdbuild.model.data.Card apply(final ExistingCard input) {
			final String className = input.getClassName();
			final org.cmdbuild.model.data.Card.Builder builder = org.cmdbuild.model.data.Card.newInstance()
					.withClassName(className) //
					.withId((input.getId() == null) ? null : input.getId().longValue());
			final Map<String, Object> attributeMap = input.getAttributes();
			for (final Entry<String, Object> entry : attributeMap.entrySet()) {
				builder.withAttribute(entry.getKey(), entry.getValue());
			}
			final org.cmdbuild.model.data.Card result = builder.build();
			return result;
		}
	};


	@Override
	public Iterable<Lookup> fetch(final QueryAllLookup queryLookup) {
		final LookupType type = LookupType.newInstance().withName(queryLookup.getType()).build();
		final boolean activeOnly = true;
		final Iterable<org.cmdbuild.data.store.lookup.Lookup> allLookup = lookupLogic.getAllLookup(type, activeOnly,
				UNUSED_LOOKUP_QUERY);
		final Iterable<Lookup> result = Iterables.transform(allLookup, STORE_TO_API_LOOKUP);
		return result;
	}

	@Override
	public Lookup fetch(final QuerySingleLookup querySingleLookup) {
		final Integer id = querySingleLookup.getId();
		final org.cmdbuild.data.store.lookup.Lookup input = lookupLogic.getLookup(Long.valueOf(id.intValue()));
		final Lookup result = STORE_TO_API_LOOKUP.apply(input);
		return result;
	}

	private final static Function<org.cmdbuild.data.store.lookup.Lookup, Lookup> STORE_TO_API_LOOKUP = //
	new Function<org.cmdbuild.data.store.lookup.Lookup, Lookup>() {
		@Override
		public Lookup apply(final org.cmdbuild.data.store.lookup.Lookup input) {
			return new LookupWrapper(input);
		}
	};
}
