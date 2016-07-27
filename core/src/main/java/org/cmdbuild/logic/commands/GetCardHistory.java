package org.cmdbuild.logic.commands;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.ClassHistory.history;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.workflow.ProcessAttributes.CurrentActivityPerformers;
import static org.cmdbuild.workflow.ProcessAttributes.FlowStatus;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.access.CardEntryFiller;
import org.cmdbuild.logic.data.access.CardStorableConverter;
import org.cmdbuild.logic.data.access.resolver.CardSerializer;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver;
import org.cmdbuild.model.data.Card;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class GetCardHistory {

	private final CMDataView dataView;

	private CMClass historyClass;

	public GetCardHistory(final CMDataView view) {
		this.dataView = view;
	}

	public Iterable<Card> exec(final Card card, final boolean allAttributes) {
		Validate.notNull(card);
		final CMClass target = dataView.findClass(card.getClassName());
		historyClass = history(target);
		final Collection<QueryAliasAttribute> attributes;
		if (allAttributes) {
			attributes = asList(anyAttribute(historyClass));
		} else {
			/*
			 * "Code" attribute is a bit special meaning but it can be disabled.
			 * When "Code" is disabled it's not added to the query attributes.
			 */
			attributes = newArrayList();
			final boolean hasCode = target.getAttribute(target.getCodeAttributeName()) != null;
			if (dataView.getActivityClass().isAncestorOf(target)) {
				if (hasCode) {
					attributes.add(attribute(historyClass, target.getCodeAttributeName()));
				}
				attributes.add(attribute(historyClass, FlowStatus.dbColumnName()));
				attributes.add(attribute(historyClass, CurrentActivityPerformers.dbColumnName()));
			} else {
				if (hasCode) {
					attributes.add(attribute(historyClass, target.getCodeAttributeName()));
				}
			}
			if (attributes.isEmpty()) {
				/*
				 * When no attributes has been added, then adds the first active
				 * attribute or all attributes (leaving the query generator to
				 * handle the issue).
				 */
				final Optional<? extends CMAttribute> firstAttribute = from(target.getActiveAttributes()).first();
				if (firstAttribute.isPresent()) {
					attributes.add(attribute(historyClass, firstAttribute.get().getName()));
				} else {
					attributes.add(anyAttribute(historyClass));
				}
			}
		}
		final CMQueryResult historyCardsResult = dataView.select(attributes.toArray()) //
				.from(historyClass) //
				.where(condition(attribute(historyClass, "CurrentId"), eq(card.getId()))) //
				.run();
		return createResponse(historyCardsResult);
	}

	private Iterable<Card> createResponse(final Iterable<CMQueryRow> rows) {
		final List<CMCard> cards = newArrayList();
		for (final CMQueryRow row : rows) {
			final CMCard card = row.getCard(historyClass);
			cards.add(card);
		}

		final Iterable<CMCard> cardsWithForeingReferences = ForeignReferenceResolver.<CMCard> newInstance() //
				.withEntries(cards) //
				.withEntryFiller(CardEntryFiller.newInstance() //
						.includeSystemAttributes(true) //
						.build()) //
				.withSerializer(new CardSerializer<CMCard>()) //
				.withMinimumAttributes(true) //
				.build() //
				.resolve();

		return from(cardsWithForeingReferences) //
				.transform(new Function<CMCard, Card>() {

					@Override
					public Card apply(final CMCard input) {
						return CardStorableConverter.of(input).convert(input);
					}

				});
	}

}
