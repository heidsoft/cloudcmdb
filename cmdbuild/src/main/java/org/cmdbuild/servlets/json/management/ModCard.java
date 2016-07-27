package org.cmdbuild.servlets.json.management;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.filterKeys;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Ordering.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.DomainHistory.history;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVITY_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.BEGIN_DATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARD;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARDS;
import static org.cmdbuild.servlets.json.CommunicationConstants.CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CONFIRMED;
import static org.cmdbuild.servlets.json.CommunicationConstants.COUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESTINATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESTINATION_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DETAIL_CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.DETAIL_CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_DIRECTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_LIMIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DOMAIN_SOURCE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.END_DATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.FUNCTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.CommunicationConstants.MASTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.MASTER_CARD_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.MASTER_CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.OUT_OF_FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.PARAMS;
import static org.cmdbuild.servlets.json.CommunicationConstants.PERFORMERS;
import static org.cmdbuild.servlets.json.CommunicationConstants.POSITION;
import static org.cmdbuild.servlets.json.CommunicationConstants.RELATION_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.RETRY_WITHOUT_FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.SORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.START;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.STATUS;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER;
import static org.cmdbuild.servlets.json.CommunicationConstants.VALUES;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;
import static org.cmdbuild.workflow.ProcessAttributes.CurrentActivityPerformers;
import static org.cmdbuild.workflow.ProcessAttributes.FlowStatus;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.ConsistencyException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.QueryOptions.QueryOptionsBuilder;
import org.cmdbuild.logic.data.access.CMCardWithPosition;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.AbstractJsonResponseSerializer;
import org.cmdbuild.servlets.json.serializers.JsonGetRelationListResponse;
import org.cmdbuild.servlets.json.serializers.LookupSerializer;
import org.cmdbuild.servlets.json.serializers.RelationAttributeSerializer;
import org.cmdbuild.servlets.json.serializers.RelationAttributeSerializer.Callback;
import org.cmdbuild.servlets.json.util.FlowStatusFilterElementGetter;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class ModCard extends JSONBaseWithSpringContext {

	private static class FilterAttribute implements Function<Card, Card> {

		private final Iterable<String> whitelist;

		public FilterAttribute(final Iterable<String> whitelist) {
			this.whitelist = whitelist;
		}

		@Override
		public Card apply(final Card input) {
			if (!isEmpty(whitelist)) {
				final Collection<String> collection = newArrayList(whitelist);
				final Map<String, Object> map = input.getAttributes();
				final Map<String, Object> filteredMap = filterKeys(map, not(in(collection)));
				final Collection<String> removed = newArrayList(filteredMap.keySet());
				for (final String remove : removed) {
					map.remove(remove);
				}
			}
			return input;
		}

	}

	private static interface JsonEntry {

		@JsonProperty(ID)
		Long getId();

		@JsonProperty(BEGIN_DATE)
		String getBegiDate();

		@JsonProperty(END_DATE)
		String getEndDate();

		@JsonProperty(USER)
		String getUser();

	}

	private static interface JsonCard extends JsonEntry {

		@JsonProperty(CLASS_NAME)
		String getClassName();

	}

	private static class JsonCardSimple extends AbstractJsonResponseSerializer implements JsonCard {

		private final Card delegate;

		public JsonCardSimple(final Card delegate) {
			this.delegate = delegate;
		}

		@Override
		public Long getId() {
			return delegate.getId();
		}

		@Override
		public String getBegiDate() {
			return formatDateTime(delegate.getBeginDate());
		}

		@Override
		public String getEndDate() {
			return formatDateTime(delegate.getEndDate());
		}

		@Override
		public String getUser() {
			return delegate.getUser();
		}

		@Override
		public String getClassName() {
			return delegate.getClassName();
		}

	}

	private static Function<Card, JsonCardSimple> CARD_JSON_CARD = new Function<Card, JsonCardSimple>() {

		@Override
		public JsonCardSimple apply(final Card input) {
			return new JsonCardSimple(input);
		}

	};

	private static interface JsonInstance extends JsonCard {

		@JsonProperty(ACTIVITY_NAME)
		String getActivityName();

		@JsonProperty(PERFORMERS)
		Collection<String> getPerformers();

		@JsonProperty(STATUS)
		Map<String, Object> getStatus();

	}

	private static class JsonInstanceSimple extends JsonCardSimple implements JsonInstance {

		private final Card delegate;
		private final LookupSerializer lookupSerializer;

		public JsonInstanceSimple(final Card delegate, final LookupSerializer lookupSerializer) {
			super(delegate);
			this.delegate = delegate;
			this.lookupSerializer = lookupSerializer;
		}

		@Override
		public String getActivityName() {
			return delegate.getAttribute(delegate.getType().getCodeAttributeName(), String.class);
		}

		@Override
		public Collection<String> getPerformers() {
			final String[] performers = delegate.getAttribute(CurrentActivityPerformers.dbColumnName(), String[].class);
			return asList(defaultIfNull(performers, new String[] {}));
		}

		@Override
		public Map<String, Object> getStatus() {
			final LookupValue status = delegate.getAttribute(FlowStatus.dbColumnName(), LookupValue.class);
			return lookupSerializer.serializeLookupValue(status);
		}

	}

	private static class CardToJsonInstance implements Function<Card, JsonInstanceSimple> {

		private final LookupSerializer lookupSerializer;

		public CardToJsonInstance(final LookupSerializer lookupSerializer) {
			this.lookupSerializer = lookupSerializer;
		}

		@Override
		public JsonInstanceSimple apply(final Card input) {
			return new JsonInstanceSimple(input, lookupSerializer);
		}

	};

	private static class JsonCardFull extends JsonCardSimple {

		private final Card delegate;
		private final LookupSerializer lookupSerializer;

		public JsonCardFull(final Card delegate, final LookupSerializer lookupSerializer) {
			super(delegate);
			this.delegate = delegate;
			this.lookupSerializer = lookupSerializer;
		}

		@JsonProperty(VALUES)
		public Map<String, Object> getValues() {
			return transformValues(delegate.getAttributes(), new Function<Object, Object>() {

				@Override
				public Object apply(final Object input) {
					Object output;
					if (input instanceof LookupValue) {
						output = lookupSerializer.serializeLookupValue((LookupValue) input);
					} else if (input instanceof IdAndDescription) {
						final IdAndDescription idAndDescription = IdAndDescription.class.cast(input);
						final Map<String, Object> map = newHashMap();
						map.put(ID, idAndDescription.getId());
						map.put(DESCRIPTION, idAndDescription.getDescription());
						output = map;
					} else {
						output = input;
					}
					return output;
				}

			});
		}

	}

	private static interface JsonRelation extends JsonEntry {

		@JsonProperty(DOMAIN)
		String getDomain();

		/**
		 * @deprecated it's an example of client-oriented development.
		 */
		@Deprecated
		@JsonProperty(DESTINATION_DESCRIPTION)
		String getDestinationDescription();

	}

	private static class JsonRelationSimple extends AbstractJsonResponseSerializer implements JsonRelation {

		private final RelationInfo delegate;

		public JsonRelationSimple(final RelationInfo delegate) {
			this.delegate = delegate;
		}

		@Override
		public Long getId() {
			return delegate.getRelationId();
		}

		@Override
		public String getBegiDate() {
			return formatDateTime(delegate.getRelationBeginDate());
		}

		@Override
		public String getEndDate() {
			return formatDateTime(delegate.getRelationEndDate());
		}

		@Override
		public String getUser() {
			return delegate.getRelation().getUser();
		}

		@Override
		public String getDomain() {
			return delegate.getQueryDomain().getDomain().getName();
		}

		@Override
		public String getDestinationDescription() {
			return delegate.getTargetDescription();
		}

	}

	private static Function<RelationInfo, JsonRelationSimple> RELATION_INFO_TO_JSON_RELATION = new Function<RelationInfo, JsonRelationSimple>() {

		@Override
		public JsonRelationSimple apply(final RelationInfo input) {
			return new JsonRelationSimple(input);
		}

	};

	private static class JsonElements<T> {

		private final List<T> elements;

		public JsonElements(final Iterable<? extends T> elements) {
			this.elements = newArrayList(elements);
		}

		@JsonProperty(ELEMENTS)
		public List<T> getElements() {
			return elements;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class JsonRelationFull extends JsonRelationSimple {

		private final RelationInfo delegate;
		private final RelationAttributeSerializer attributeSerializer;

		public JsonRelationFull(final RelationInfo delegate, final RelationAttributeSerializer attributeSerializer) {
			super(delegate);
			this.delegate = delegate;
			this.attributeSerializer = attributeSerializer;
		}

		@JsonProperty(SOURCE)
		public Long getSource() {
			return delegate.getSourceId();
		}

		@JsonProperty(SOURCE_DESCRIPTION)
		public String getSourceDescription() {
			return delegate.getSourceDescription();
		}

		@JsonProperty(DESTINATION)
		public Long getDestination() {
			return delegate.getTargetId();
		}

		@JsonProperty(VALUES)
		public Map<String, Object> getValues() {
			final Map<String, Object> values = newHashMap();
			attributeSerializer.toClient(delegate, new Callback() {

				@Override
				public void handle(final String name, final Object value) {
					values.put(name, value);
				}

			});
			return values;
		}

	}

	/**
	 * Retrieves the cards for the specified class. If a filter is defined, only
	 * the cards that match the filter are retrieved. The fetched cards are
	 * sorted if a sorter is defined. Note that the max number of retrieved
	 * cards is the 'limit' parameter
	 * 
	 * @param className
	 *            the name of the class for which I want to retrieve the cards
	 * @param filter
	 *            null if no filter is defined. It retrieves all the active
	 *            cards for the specified class that match the filter
	 * @param limit
	 *            max number of retrieved cards (for pagination it is the max
	 *            number of cards in a page)
	 * @param offset
	 *            is the offset from the first card (for pagination)
	 * @param sorters
	 *            null if no sorter is defined
	 */
	@JSONExported
	public JSONObject getCardList( //
			@Parameter(value = CLASS_NAME, required = false) final String className, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			final Map<String, Object> otherAttributes //
	) throws JSONException {
		return getCardList(className, filter, limit, offset, sorters, null, otherAttributes);
	}

	/**
	 * Retrieves a list of cards for the specified class, returning only the
	 * values for a subset of values
	 * 
	 * @param filter
	 *            null if no filter is specified
	 * @param sorters
	 *            null if no sorter is specified
	 * @param attributes
	 *            null if all attributes for the specified class are required
	 *            (it is equivalent to the getCardList method)
	 * @return
	 */
	@JSONExported
	// TODO: check the input parameters and serialization
	public JSONObject getCardListShort( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = ATTRIBUTES, required = false) final JSONArray attributes, //
			final Map<String, Object> otherAttributes //
	) throws JSONException, CMDBException {
		JSONArray attributesToSerialize = new JSONArray();
		if (attributes == null || attributes.length() == 0) {
			attributesToSerialize.put(DESCRIPTION_ATTRIBUTE);
			attributesToSerialize.put(ID_ATTRIBUTE);
		} else {
			attributesToSerialize = attributes;
		}

		return getCardList(className, filter, limit, offset, sorters, attributesToSerialize, otherAttributes);
	}

	/**
	 * Retrieves the cards for the specified class. If a filter is defined, only
	 * the cards that match the filter are retrieved. The fetched cards are
	 * sorted if a sorter is defined. Note that the max number of retrieved
	 * cards is the 'limit' parameter
	 * 
	 * @param className
	 *            the name of the class for which I want to retrieve the cards
	 * @param filter
	 *            null if no filter is defined. It retrieves all the active
	 *            cards for the specified class that match the filter
	 * @param limit
	 *            max number of retrieved cards (for pagination it is the max
	 *            number of cards in a page)
	 * @param offset
	 *            is the offset from the first card (for pagination)
	 * @param sorters
	 *            null if no sorter is defined
	 */
	@JSONExported
	public JSONObject getDetailList( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(LIMIT) final int limit, //
			@Parameter(START) final int offset, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			final Map<String, Object> otherAttributes //
	) throws JSONException {

		final DataAccessLogic dataLogic = userDataAccessLogic();
		final QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.parameters(otherAttributes) //
				.filter(filter); //

		final QueryOptions queryOptions = queryOptionsBuilder.build();
		final PagedElements<Card> response = dataLogic.fetchCards(className, queryOptions);
		return cardSerializer().toClient(response.elements(), response.totalSize());
	}

	private JSONObject getCardList(final String className, final JSONObject filter, final int limit, final int offset,
			final JSONArray sorters, final JSONArray attributes, final Map<String, Object> otherAttributes)
			throws JSONException {

		final DataAccessLogic dataLogic = userDataAccessLogic();
		final QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.onlyAttributes(toIterable(attributes)) //
				.parameters(otherAttributes) //
				.filter(filter);

		final QueryOptions queryOptions = queryOptionsBuilder.build();
		final PagedElements<Card> response = dataLogic.fetchCards(className, queryOptions);
		return cardSerializer().toClient(removeUnwantedAttributes(response.elements(), attributes),
				response.totalSize());
	}

	private Iterable<Card> removeUnwantedAttributes(final Iterable<Card> elements, final JSONArray attributes) {
		return from(elements) //
				.transform(new FilterAttribute(toIterable(attributes)));
	}

	@JSONExported
	public JSONObject getSQLCardList( //
			final @Parameter(FUNCTION) String functionName, //
			final @Parameter(START) int offset, //
			final @Parameter(LIMIT) int limit, //
			final @Parameter(value = PARAMS, required = false) JSONObject jsonParameters, //
			final @Parameter(value = FILTER, required = false) JSONObject filter, //
			final @Parameter(value = SORT, required = false) JSONArray sorters //
	) throws JSONException { //
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.parameters(toMap(jsonParameters)) //
				.build();

		final PagedElements<Card> response = systemDataAccessLogic().fetchSQLCards(functionName, queryOptions);
		return cardSerializer().toClient(response.elements(), response.totalSize(), CARDS);
	}

	@JSONExported
	public JSONObject getCard( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId //
	) throws JSONException {
		final Card fetchedCard = userDataAccessLogic().fetchCard(className, cardId);
		return cardSerializer().toClient(fetchedCard, CARD);
	}

	@JSONExported
	public JsonResponse getHistoricCard( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId //
	) {
		final Card card = userDataAccessLogic().fetchHistoricCard(className, cardId);
		return JsonResponse.success(new JsonCardFull(card, lookupSerializer()));
	}

	@JSONExported
	public JSONObject getCardPosition( //
			@Parameter(value = RETRY_WITHOUT_FILTER, required = false) final boolean retryWithoutFilter, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = SORT, required = false) final JSONArray sorters, //
			@Parameter(value = STATE, required = false) final String flowStatus //
	) throws JSONException {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataAccessLogic = userDataAccessLogic();

		QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption();
		addFilterToQueryOption(new JsonFilterHelper(filter) //
				.merge(new FlowStatusFilterElementGetter(lookupHelper(), flowStatus)), queryOptionsBuilder);
		addSortersToQueryOptions(sorters, queryOptionsBuilder);

		CMCardWithPosition card = dataAccessLogic.getCardPosition(className, cardId, queryOptionsBuilder.build());

		if (card.hasNoPosition() && retryWithoutFilter) {
			out.put(OUT_OF_FILTER, true);
			queryOptionsBuilder = QueryOptions.newQueryOption();
			final CMCard expectedCard = dataAccessLogic.fetchCMCard(className, cardId);
			final String flowStatusForExpectedCard = flowStatus(expectedCard);
			if (flowStatusForExpectedCard != null) {
				addFilterToQueryOption(new JsonFilterHelper(new JSONObject()) //
						.merge(new FlowStatusFilterElementGetter(lookupHelper(), flowStatusForExpectedCard)),
						queryOptionsBuilder);
			}
			addSortersToQueryOptions(sorters, queryOptionsBuilder);
			card = dataAccessLogic.getCardPosition(className, expectedCard.getId(), queryOptionsBuilder.build());
		}

		out.put(POSITION, card.getPosition());
		/*
		 * FIXME It's late. We need the flow status if ask for a process
		 * position. Do it in a better way!
		 */
		if (card.isFound()) {
			final Object retrievedFlowStatus = card.getAttribute(FlowStatus.dbColumnName());
			if (retrievedFlowStatus != null) {
				final Lookup lookupFlowStatus = lookupLogic().getLookup(((LookupValue) retrievedFlowStatus).getId());
				out.put("FlowStatus", lookupFlowStatus.code());
			}
		}

		return out;
	}

	private String flowStatus(final CMCard card) {
		final Object retrievedFlowStatus = card.get(FlowStatus.dbColumnName());
		final String output;
		if (retrievedFlowStatus != null) {
			final Lookup lookupFlowStatus = lookupLogic().getLookup(((LookupValue) retrievedFlowStatus).getId());
			output = lookupFlowStatus.code();
		} else {
			output = null;
		}
		return output;
	}

	private void addFilterToQueryOption(final JSONObject filter, final QueryOptionsBuilder queryOptionsBuilder) {
		if (filter != null) {
			queryOptionsBuilder.filter(filter);
		}
	}

	private void addSortersToQueryOptions(final JSONArray sorters, final QueryOptionsBuilder queryOptionsBuilder) {
		if (sorters != null) {
			queryOptionsBuilder.orderBy(sorters); //
		}
	}

	@JSONExported
	public JSONObject updateCard( //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) Long cardId, //
			final Map<String, Object> attributes //
	) throws Exception {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final Card cardToBeCreatedOrUpdated = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.withUser(operationUser().getAuthenticatedUser().getUsername()) //
				.withAllAttributes(attributes) //
				.build();

		final boolean cardMustBeCreated = cardId == -1;

		if (cardMustBeCreated) {
			cardId = dataLogic.createCard(cardToBeCreatedOrUpdated);
			out.put("id", cardId);
		} else {
			try {
				dataLogic.updateCard(cardToBeCreatedOrUpdated);
			} catch (final ConsistencyException e) {
				notifier().warn(e);
				out.put("success", false);
			}
		}

		try {
			final Card card = dataLogic.fetchCard(className, cardId);
			updateGisFeatures(card, attributes);
		} catch (final NotFoundException ex) {
			logger.warn("The card with id " + cardId
					+ " is not present in the database or the logged user can not see it");
		}

		return out;
	}

	private void updateGisFeatures(final Card card, final Map<String, Object> attributes) throws Exception {
		final GISLogic gisLogic = gisLogic();
		if (gisLogic.isGisEnabled()) {
			gisLogic.updateFeatures(card, attributes);
		}
	}

	@JSONExported
	public JSONObject bulkUpdate( //
			final Map<String, Object> attributes, //
			@Parameter(value = CARDS, required = false) final JSONArray cards, //
			@Parameter(value = CONFIRMED, required = false) final boolean confirmed //
	) throws JSONException {
		final JSONObject out = new JSONObject();
		if (!confirmed) { // needs confirmation from user
			return out.put(COUNT, cards.length());
		}
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final Map<Long, String> cardIdToClassName = extractCardsFromJsonArray(cards);
		attributes.remove(CARDS);
		attributes.remove(CONFIRMED);
		dataLogic.updateCards(from(cardIdToClassName.entrySet()) //
				.transform(new Function<Entry<Long, String>, Card>() {

					@Override
					public Card apply(final Entry<Long, String> input) {
						return Card.newInstance() //
								.withId(input.getKey()) //
								.withClassName(input.getValue()).withAllAttributes(attributes) //
								.withUser(operationUser().getAuthenticatedUser().getUsername()) //
								.build();
					}

				}));
		return out;
	}

	@JSONExported
	public JSONObject bulkUpdateFromFilter( //
			final Map<String, Object> attributes, //
			@Parameter(value = CLASS_NAME, required = false) final String className, //
			@Parameter(value = CARDS, required = false) final JSONArray cards, //
			@Parameter(value = FILTER, required = false) final JSONObject filter, //
			@Parameter(value = CONFIRMED, required = false) final boolean confirmed //
	) throws JSONException {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.filter(filter) //
				.build();
		final PagedElements<Card> response = dataLogic.fetchCards(className, queryOptions);
		if (!confirmed) {
			final int numberOfCardsToUpdate = response.totalSize() - cards.length();
			return out.put(COUNT, numberOfCardsToUpdate);
		}
		final Iterable<Card> fetchedCards = response.elements();
		attributes.remove(CLASS_NAME);
		attributes.remove(CARDS);
		attributes.remove(FILTER);
		attributes.remove(CONFIRMED);
		for (final Card cardToUpdate : fetchedCards) {
			if (cardNeedToBeUpdated(cards, cardToUpdate.getId())) {
				dataLogic.updateFetchedCard(cardToUpdate, attributes);
			}
		}
		return out;
	}

	private boolean cardNeedToBeUpdated(final JSONArray cardsNotToUpdate, final Long cardId) throws JSONException {
		final Map<Long, String> cardIdToClassName = extractCardsFromJsonArray(cardsNotToUpdate);
		final String className = cardIdToClassName.get(cardId);
		return className == null;
	}

	@JSONExported
	public JSONObject deleteCard( //
			@Parameter(value = "Id") final Long cardId, @Parameter(value = "IdClass") final Long classId)
			throws CMDBException {
		final JSONObject out = new JSONObject();
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final CMClass found = dataLogic.findClass(classId);
		if (found == null) {
			throw NotFoundException.NotFoundExceptionType.CLASS_NOTFOUND.createException(classId.toString());
		}
		final String className = found.getIdentifier().getLocalName();
		dataLogic.deleteCard(className, cardId);

		return out;
	}

	@JSONExported
	public JsonResponse getCardHistory(//
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId //
	) {
		final Card src = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		final Iterable<Card> response = userDataAccessLogic().getCardHistory(src, false);
		final Iterable<Card> ordered = from( //
				new Comparator<Card>() {

					@Override
					public int compare(final Card o1, final Card o2) {
						return o1.getBeginDate().compareTo(o2.getBeginDate());
					}

				}) //
				.reverse() //
				.immutableSortedCopy(response);
		return JsonResponse.success(new JsonElements<JsonCardSimple>(from(ordered).transform(CARD_JSON_CARD)));
	}

	@JSONExported
	public JsonResponse getProcessHistory(//
			@Parameter(value = CLASS_NAME) final String processClassName, //
			@Parameter(value = CARD_ID) final Long processInstaceId //
	) {
		final Card src = Card.newInstance() //
				.withClassName(processClassName) //
				.withId(processInstaceId) //
				.build();
		final Iterable<Card> response = userDataAccessLogic().getCardHistory(src, false);
		final Iterable<Card> ordered = from( //
				new Comparator<Card>() {

					@Override
					public int compare(final Card o1, final Card o2) {
						return o1.getBeginDate().compareTo(o2.getBeginDate());
					}

				}) //
				.reverse() //
				.immutableSortedCopy(response);
		return JsonResponse.success(new JsonElements<JsonInstanceSimple>(from(ordered).transform(
				new CardToJsonInstance(lookupSerializer()))));
	}

	/*
	 * Relations
	 */

	@JSONExported
	public JSONObject getRelationList( //
			@Parameter(value = CARD_ID) final Long cardId, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = DOMAIN_LIMIT, required = false) final int domainlimit, //
			@Parameter(value = DOMAIN_ID, required = false) final Long domainId, //
			@Parameter(value = DOMAIN_SOURCE, required = false) final String querySource //
	) throws JSONException {
		final DataAccessLogic dataAccesslogic = userDataAccessLogic();
		final Card src = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		final DomainWithSource dom = DomainWithSource.create(domainId, querySource);
		final GetRelationListResponse out = dataAccesslogic.getRelationListEmptyForWrongId(src, dom);
		return new JsonGetRelationListResponse(out, domainlimit, relationAttributeSerializer()).toJson();
	}

	@JSONExported
	public JsonResponse getRelationsHistory(//
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARD_ID) final Long cardId //
	) {
		final Card src = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		final GetRelationHistoryResponse response = userDataAccessLogic().getRelationHistory(src);
		final Iterable<RelationInfo> ordered = from( //
				new Comparator<RelationInfo>() {

					@Override
					public int compare(final RelationInfo o1, final RelationInfo o2) {
						return o1.getRelationBeginDate().compareTo(o2.getRelationBeginDate());
					}

				}) //
				.reverse() //
				.immutableSortedCopy(response);
		return JsonResponse.success(new JsonElements<JsonRelationSimple>(from(ordered).transform(
				RELATION_INFO_TO_JSON_RELATION)));
	}

	@JSONExported
	public JsonResponse getHistoricRelation(//
			@Parameter(value = DOMAIN) final String domain, //
			@Parameter(value = ID) final Long id //
	) {
		final CMDomain _domain = userDataAccessLogic().findDomain(domain);
		final Optional<RelationInfo> relation = userDataAccessLogic().getRelation(history(_domain), id);
		return JsonResponse.success(new JsonRelationFull(relation.get(), relationAttributeSerializer()));
	}

	/**
	 * 
	 * @param domainName
	 *            is the domain between the source class and the destination
	 *            class
	 * @param master
	 *            identifies the side of the "parent" card (_1 or _2)
	 * @param attributes
	 *            are the relation attributes and the cards (id and className)
	 *            that will be created. _1 and _2 represents the source and the
	 *            destination cards
	 * @throws JSONException
	 */
	@JSONExported
	public void createRelations( //
			@Parameter(DOMAIN_NAME) final String domainName, //
			@Parameter(MASTER) final String master, //
			@Parameter(ATTRIBUTES) final JSONObject attributes //
	) throws JSONException {
		final DataAccessLogic dataAccessLogic = userDataAccessLogic();
		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.domainName = domainName;
		relationDTO.master = master;
		relationDTO.relationId = null;
		relationDTO.relationAttributeToValue = extractOnlyRelationAttributes(attributes);

		final JSONArray srcCards = attributes.getJSONArray("_1");
		final Map<Long, String> srcCardIdToClassName = extractCardsFromJsonArray(srcCards);
		relationDTO.srcCardIdToClassName = srcCardIdToClassName;

		final JSONArray dstCards = attributes.getJSONArray("_2");
		final Map<Long, String> dstCardIdToClassName = extractCardsFromJsonArray(dstCards);
		relationDTO.dstCardIdToClassName = dstCardIdToClassName;

		dataAccessLogic.createRelations(relationDTO);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractOnlyRelationAttributes(final JSONObject attributes) throws JSONException {
		final Map<String, Object> relationAttributeToValue = newHashMap();
		final Iterator<String> iterator = attributes.keys();
		while (iterator.hasNext()) {
			final String attributeName = iterator.next();
			if (!attributeName.equals("_1") && !attributeName.equals("_2")) {
				final Object attributeValue;
				if (attributes.isNull(attributeName)) {
					attributeValue = null;
				} else {
					attributeValue = attributes.get(attributeName);
				}
				relationAttributeToValue.put(attributeName, attributeValue);
			}
		}
		return relationAttributeToValue;
	}

	private Map<Long, String> extractCardsFromJsonArray(final JSONArray cards) throws JSONException {
		final Map<Long, String> cardIdToClassName = newHashMap();
		for (int i = 0; i < cards.length(); i++) {
			final JSONObject card = cards.getJSONObject(i);
			final Long cardId = card.getLong("cardId");
			final String className = card.getString("className");
			cardIdToClassName.put(cardId, className);
		}
		return cardIdToClassName;
	}

	@JSONExported
	public void modifyRelation( //
			@Parameter(RELATION_ID) final Long relationId, //
			@Parameter(DOMAIN_NAME) final String domainName, //
			@Parameter(MASTER) final String master, //
			@Parameter(ATTRIBUTES) final JSONObject attributes //
	) throws JSONException {
		final DataAccessLogic dataAccessLogic = userDataAccessLogic();

		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.relationId = relationId;
		relationDTO.domainName = domainName;
		relationDTO.master = master;
		relationDTO.relationAttributeToValue = extractOnlyRelationAttributes(attributes);
		final JSONArray srcCards = attributes.getJSONArray("_1");
		final Map<Long, String> srcCardIdToClassName = extractCardsFromJsonArray(srcCards);
		relationDTO.srcCardIdToClassName = srcCardIdToClassName;
		final JSONArray dstCards = attributes.getJSONArray("_2");
		final Map<Long, String> dstCardIdToClassName = extractCardsFromJsonArray(dstCards);
		relationDTO.dstCardIdToClassName = dstCardIdToClassName;

		dataAccessLogic.updateRelation(relationDTO);
	}

	@JSONExported
	public void deleteRelation( //
			@Parameter(DOMAIN_NAME) final String domainName, //
			@Parameter(RELATION_ID) final Long relationId //
	) {
		final DataAccessLogic dataAccessLogic = userDataAccessLogic();
		dataAccessLogic.deleteRelation(domainName, relationId);
	}

	/*
	 * If a domain name is not send, the detail is given by a foreign key
	 * attribute, so delete directly the card
	 */
	@JSONExported
	public void deleteDetail( //
			@Parameter(value = DETAIL_CLASS_NAME) final String detailClassName, //
			@Parameter(value = DETAIL_CARD_ID) final Long detailCardId, //
			@Parameter(value = MASTER_CLASS_NAME, required = false) final String masterClassName, //
			@Parameter(value = MASTER_CARD_ID, required = false) final Long masterCardId, //
			@Parameter(value = DOMAIN_NAME, required = false) final String domainName //
	) {

		final DataAccessLogic dataLogic = userDataAccessLogic();
		if (domainName != null) {
			final Card detail = Card.newInstance().withClassName(detailClassName).withId(detailCardId).build();
			final Card master = Card.newInstance().withClassName(masterClassName).withId(masterCardId).build();
			dataLogic.deleteDetail(master, detail, domainName);
		} else {
			dataLogic.deleteCard(detailClassName, detailCardId);
		}
	}

	@JSONExported
	public JsonResponse getAlreadyRelatedCards( //
			@Parameter(value = DOMAIN_NAME) final String domainName, //
			@Parameter(value = DOMAIN_DIRECTION) final String domainDirection, //
			@Parameter(value = CLASS_NAME) final String className, //
			@Parameter(value = CARDS) final JSONArray cardsIdArray //
	) throws JSONException {
		final DataAccessLogic dataLogic = userDataAccessLogic();
		final CMDomain domain = dataLogic.findDomain(domainName);

		final DomainWithSource dom = DomainWithSource.create(domain.getId(), domainDirection);

		final Predicate<Card> isCardAlreadyRelated = new Predicate<Card>() {
			@Override
			public boolean apply(final Card input) {
				final GetRelationListResponse relationList = dataLogic.getRelationList(input, dom);
				return relationList.getTotalNumberOfRelations() > 0;
			}
		};

		final Collection<Card> cardsToCheck = newArrayList();
		for (int i = 0; i < cardsIdArray.length(); i++) {
			final Card card = dataLogic.fetchCard(className, Long.parseLong(String.valueOf(cardsIdArray.get(i))));
			cardsToCheck.add(card);
		}
		final Iterable<Card> alreadyRelatedCards = filter(cardsToCheck, isCardAlreadyRelated);
		final Iterable<? extends JsonCard> alreadyRelatedJsonCards = from(alreadyRelatedCards) //
				.transform(CARD_JSON_CARD) //
				.toList();
		return JsonResponse.success(alreadyRelatedJsonCards);
	}

}
