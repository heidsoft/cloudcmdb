package org.cmdbuild.logic.data.access;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.entrytype.Deactivable.IsActivePredicate.activeOnes;
import static org.cmdbuild.dao.entrytype.Predicates.allDomains;
import static org.cmdbuild.dao.entrytype.Predicates.attributeTypeInstanceOf;
import static org.cmdbuild.dao.entrytype.Predicates.disabledClass;
import static org.cmdbuild.dao.entrytype.Predicates.domainFor;
import static org.cmdbuild.dao.entrytype.Predicates.usableForReferences;
import static org.cmdbuild.dao.guava.Functions.toValueSet;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.ClassHistory.history;
import static org.cmdbuild.dao.query.clause.Clauses.call;
import static org.cmdbuild.dao.query.clause.DomainHistory.history;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Aliases.as;
import static org.cmdbuild.dao.query.clause.alias.Aliases.name;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.data.store.Storables.storableOf;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.activation.DataHandler;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Constants;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetCardHistory;
import org.cmdbuild.logic.commands.GetRelationHistory;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.commands.GetRelationSingle;
import org.cmdbuild.logic.data.LockLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.resolver.CardSerializer;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.model.data.IdentifiedRelation;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVData;
import org.cmdbuild.servlets.json.management.dataimport.csv.CSVImporter;
import org.cmdbuild.servlets.json.management.dataimport.csv.CsvReader;
import org.cmdbuild.servlets.json.management.dataimport.csv.SuperCsvCsvReader;
import org.cmdbuild.servlets.json.management.export.CMDataSource;
import org.cmdbuild.servlets.json.management.export.DBDataSource;
import org.cmdbuild.servlets.json.management.export.DataExporter;
import org.cmdbuild.servlets.json.management.export.csv.CsvExporter;
import org.json.JSONException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.prefs.CsvPreference;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class DefaultDataAccessLogic implements DataAccessLogic {

	private static final String ID_ATTRIBUTE = org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;

	private static final Alias DOM_ALIAS = name("DOM");
	private static final Alias DST_ALIAS = name("DST");

	private static final Function<CMCard, Card> CMCARD_TO_CARD = new Function<CMCard, Card>() {
		@Override
		public Card apply(final CMCard input) {
			return CardStorableConverter.of(input).convert(input);
		}
	};

	private static final Comparator<CMAttribute> NAME_ASC = new Comparator<CMAttribute>() {

		@Override
		public int compare(final CMAttribute o1, final CMAttribute o2) {
			final int v1 = o1.getClassOrder();
			final int v2 = o2.getClassOrder();
			return (v1 < v2 ? -1 : (v1 == v2 ? 0 : 1));
		}

	};

	public static final String USER = "__user__";
	public static final String ROLE = "__role__";

	private final CMDataView systemDataView;
	private final LookupStore lookupStore;
	private final CMDataView dataView;
	private final OperationUser operationUser;
	private final LockLogic lockLogic;

	public DefaultDataAccessLogic( //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final CMDataView dataView, //
			final OperationUser operationUser, //
			final LockLogic lockLogic //
	) {
		this.systemDataView = systemDataView;
		this.dataView = dataView;
		this.lookupStore = lookupStore;
		this.operationUser = operationUser;
		this.lockLogic = lockLogic;
	}

	@Override
	public CMDataView getView() {
		return dataView;
	}

	private DataViewStore<Card> storeOf(final Card card) {
		return DataViewStore.<Card> newInstance() //
				.withDataView(dataView) //
				.withStorableConverter(CardStorableConverter.of(card)) //
				.build();
	}

	@Override
	public Map<Object, List<RelationInfo>> relationsBySource(final String sourceTypeName, final DomainWithSource dom) {
		return new GetRelationList(dataView).list(sourceTypeName, dom);
	}

	@Override
	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom,
			final QueryOptions options) {
		return new GetRelationList(dataView).exec(srcCard, dom, options);
	}

	@Override
	public GetRelationListResponse getRelationList(final Card srcCard, final DomainWithSource dom) {
		return new GetRelationList(dataView).exec(srcCard, dom, QueryOptions.newQueryOption().build());
	}

	@Override
	public GetRelationListResponse getRelationListEmptyForWrongId(final Card srcCard, final DomainWithSource dom) {
		return new GetRelationList(dataView).emptyForWrongId().exec(srcCard, dom,
				QueryOptions.newQueryOption().build());
	}

	@Override
	public GetRelationListResponse getRelationList(final CMDomain domain, final QueryOptions queryOptions) {
		return new GetRelationList(dataView).exec(domain, queryOptions);
	}

	@Override
	public Optional<RelationInfo> getRelation(final String domain, final Long id) {
		final CMDomain _domain = dataView.findDomain(domain);
		return getRelation(_domain, id);
	}

	@Override
	public Optional<RelationInfo> getRelation(final CMDomain domain, final Long id) {
		return new GetRelationSingle(dataView).exec(domain, id);
	}

	@Override
	public GetRelationHistoryResponse getRelationHistory(final Card srcCard) {
		return new GetRelationHistory(dataView).exec(srcCard);
	}

	@Override
	public GetRelationHistoryResponse getRelationHistory(final Card srcCard, final CMDomain domain) {
		return new GetRelationHistory(dataView).exec(srcCard, domain);
	}

	@Override
	public Optional<RelationInfo> getHistoricRelation(final String domain, final Long id) {
		final CMDomain target = dataView.findDomain(domain);
		return new GetRelationSingle(dataView).exec(history(target), id);
	}

	@Override
	public Iterable<Card> getCardHistory(final Card srcCard, final boolean allAttributes) {
		return new GetCardHistory(dataView).exec(srcCard, allAttributes);
	}

	@Override
	public Card fetchHistoricCard(final String className, final Long cardId) {
		final CMClass entryType = dataView.findClass(className);
		return from(asList(fetchCMCard(history(entryType), cardId))) //
				.transform(CMCARD_TO_CARD) //
				.first() //
				.get();
	}

	@Override
	public CMClass findClass(final Long classId) {
		return dataView.findClass(classId);
	}

	@Override
	public CMClass findClass(final String className) {
		return dataView.findClass(className);
	}

	@Override
	public boolean hasClass(final Long classId) {
		return findClass(classId) != null;
	}

	@Override
	public CMDomain findDomain(final Long domainId) {
		return dataView.findDomain(domainId);
	}

	@Override
	public CMDomain findDomain(final String domainName) {
		return dataView.findDomain(domainName);
	}

	/**
	 *
	 * @return only active classes (all classes, included superclasses, simple
	 *         classes and process classes).
	 */
	@Override
	public Iterable<? extends CMClass> findActiveClasses() {
		return from(dataView.findClasses()) //
				.filter(activeOnes());
	}

	/**
	 *
	 * @return active and non active domains
	 */
	@Override
	public Iterable<? extends CMDomain> findAllDomains() {
		return dataView.findDomains();
	}

	/**
	 *
	 * @return only active domains
	 */
	@Override
	public Iterable<? extends CMDomain> findActiveDomains() {
		return from(dataView.findDomains()) //
				.filter(activeOnes());
	}

	@Override
	public Iterable<? extends CMDomain> findReferenceableDomains(final String className) {
		final CMClass fetchedClass = dataView.findClass(className);
		return from(dataView.findDomains()) //
				.filter(domainFor(fetchedClass)) //
				.filter(not(disabledClass(fetchedClass))) //
				.filter(usableForReferences(fetchedClass));
	}

	@Override
	public Iterable<? extends CMClass> findAllClasses() {
		return dataView.findClasses();
	}

	@Override
	public Iterable<? extends CMClass> findClasses(final boolean activeOnly) {
		final Iterable<? extends CMClass> fetchedClasses = activeOnly ? findActiveClasses() : findAllClasses();
		final Iterable<? extends CMClass> nonProcessClasses = filter(fetchedClasses, nonProcessClasses());
		final Iterable<? extends CMClass> classesToBeReturned = activeOnly
				? filter(nonProcessClasses, nonSystemButUsable()) : nonProcessClasses;
		return classesToBeReturned;
	}

	private Predicate<CMClass> nonProcessClasses() {
		final CMClass processBaseClass = findClass(Constants.BASE_PROCESS_CLASS_NAME);
		final Predicate<CMClass> nonProcessClasses = new Predicate<CMClass>() {
			@Override
			public boolean apply(final CMClass input) {
				return !processBaseClass.isAncestorOf(input);
			}
		};
		return nonProcessClasses;
	}

	/**
	 *
	 * @return a predicate that will filter classes whose mode does not start
	 *         with sys... (e.g. sysread or syswrite)
	 */
	private Predicate<CMClass> nonSystemButUsable() {
		final Predicate<CMClass> predicate = new Predicate<CMClass>() {
			@Override
			public boolean apply(final CMClass input) {
				return !input.isSystemButUsable();
			}
		};
		return predicate;
	}

	@Override
	public PagedElements<CMAttribute> getAttributes(final String className, final boolean onlyActive,
			final AttributesQuery attributesQuery) {
		final CMClass target = findClass(className);
		final Iterable<? extends CMAttribute> elements = onlyActive ? target.getActiveAttributes()
				: target.getAttributes();
		final Iterable<? extends CMAttribute> ordered = Ordering.from(NAME_ASC).sortedCopy(elements);
		final Integer offset = attributesQuery.offset();
		final Integer limit = attributesQuery.limit();
		final FluentIterable<CMAttribute> limited = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(Functions.<CMAttribute> identity());
		return new PagedElements<CMAttribute>(limited, size(elements));
	}

	@Override
	public PagedElements<CMAttribute> getDomainAttributes(final String className, final boolean onlyActive,
			final AttributesQuery attributesQuery) {
		final CMDomain target = findDomain(className);
		final Iterable<? extends CMAttribute> elements = onlyActive ? target.getActiveAttributes()
				: target.getAttributes();
		final Iterable<? extends CMAttribute> ordered = Ordering.from(NAME_ASC).sortedCopy(elements);
		final Integer offset = attributesQuery.offset();
		final Integer limit = attributesQuery.limit();
		final FluentIterable<CMAttribute> limited = from(ordered) //
				.skip((offset == null) ? 0 : offset) //
				.limit((limit == null) ? Integer.MAX_VALUE : limit) //
				.transform(Functions.<CMAttribute> identity());
		return new PagedElements<CMAttribute>(limited, size(elements));
	}

	/**
	 * Fetches the card with the specified Id from the class with the specified
	 * name
	 *
	 * @param className
	 * @param cardId
	 * @throws NoSuchElementException
	 *             if the card with the specified Id number does not exist or it
	 *             is not unique
	 * @return the card with the specified Id.
	 */
	@Override
	public Card fetchCard(final String className, final Long cardId) {
		return from(asList(fetchCMCard(className, cardId))) //
				.transform(CMCARD_TO_CARD) //
				.first() //
				.get();
	}

	@Override
	public CMCard fetchCMCard(final String className, final Long cardId) {
		final CMClass entryType = dataView.findClass(className);
		return fetchCMCard(entryType, cardId);
	}

	private CMCard fetchCMCard(final CMClass entryType, final Long cardId) {
		try {
			final CMQueryRow row = dataView.select(anyAttribute(entryType)) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), eq(cardId))) //
					.limit(1) //
					.skipDefaultOrdering() //
					.run() //
					.getOnlyRow();
					/**
					 * FIXME: delete it when ForeignReferenceResolver will be
					 * unused.
					 */
					final Iterable<CMCard> cards = ForeignReferenceResolver.<CMCard> newInstance() //
							.withEntries(asList(row.getCard(entryType))) //
							.withEntryFiller(CardEntryFiller.newInstance() //
									.build()) //
							.withSerializer(new CardSerializer<CMCard>()) //
							.build() //
							.resolve();

			return from(cards) //
					.first() //
					.get();
		} catch (final NoSuchElementException ex) {
			throw NotFoundExceptionType.CARD_NOTFOUND.createException(entryType.getName());
		}
	}

	@Override
	public Card fetchCardShort(final String className, final Long cardId, final QueryOptions queryOptions) {
		final CMClass entryType = dataView.findClass(className);
		final List<QueryAliasAttribute> attributesToDisplay = Lists.newArrayList();

		for (final String attribute : queryOptions.getAttributes()) {
			final QueryAliasAttribute queryAttribute = attribute(entryType, attribute);
			attributesToDisplay.add(queryAttribute);
		}

		try {
			final CMQueryRow row = dataView.select(attributesToDisplay.toArray()) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), eq(cardId))) //
					.limit(1) //
					.skipDefaultOrdering() //
					.run() //
					.getOnlyRow();

			final CMCard card = row.getCard(entryType);
			final CMCard cardWithResolvedReference = resolveCardReferences(entryType, card);

			return CMCARD_TO_CARD.apply(cardWithResolvedReference);

		} catch (final NoSuchElementException ex) {
			throw NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
	}

	/**
	 * @param entryType
	 * @param card
	 * @return
	 */
	@Override
	public CMCard resolveCardReferences( //
			final CMClass entryType, final CMCard card //
	) {
		final Iterable<CMCard> cardWithResolvedReference = ForeignReferenceResolver.<CMCard> newInstance() //
				.withEntries(asList(card)) //
				.withEntryFiller(CardEntryFiller.newInstance() //
						.build()) // /
				.withSerializer(new CardSerializer<CMCard>()) //
				.build() //
				.resolve();

		return cardWithResolvedReference.iterator().next();
	}

	@Override
	public Card fetchCard(final Long classId, final Long cardId) {
		final CMClass entryType = dataView.findClass(classId);
		return fetchCard(entryType.getIdentifier().getLocalName(), cardId);
	}

	/**
	 * Retrieve the cards of a given class that matches the given query options
	 *
	 * @param className
	 * @param queryOptions
	 * @return a FetchCardListResponse
	 */
	@Override
	public PagedElements<Card> fetchCards(final String className, final QueryOptions queryOptions) {
		/*
		 * preferred solution to avoid pre-release errors
		 */
		final PagedElements<Card> output;
		if (isNotBlank(className)) {
			output = fetchCardsWithClassName(className, queryOptions);
		} else {
			output = fetchCardsWithoutClassName(queryOptions);
		}
		return output;
	}

	private PagedElements<Card> fetchCardsWithClassName(final String className, final QueryOptions queryOptions) {
		final CMClass fetchedClass = dataView.findClass(className);
		final PagedElements<CMCard> fetchedCards = DataViewCardFetcher.newInstance() //
				.withDataView(dataView) //
				.withClassName(className) //
				.withQueryOptions(queryOptions) //
				.build() //
				.fetch();
		final Iterable<Card> cards = resolveCardForeignReferences(fetchedClass, fetchedCards);
		return new PagedElements<Card>(cards, fetchedCards.totalSize());
	}

	/**
	 * @param fetchedClass
	 *            CMClass
	 * @param fetchedCards
	 *            PagedElements<CMCard>
	 * @return
	 */
	private Iterable<CMCard> resolveCMCardForeignReferences(final CMClass fetchedClass,
			final Iterable<CMCard> fetchedCards) {
		final Iterable<CMCard> cardsWithForeingReferences = ForeignReferenceResolver.<CMCard> newInstance() //
				.withEntries(fetchedCards) //
				.withEntryFiller(CardEntryFiller.newInstance() //
						.build()) //
				.withSerializer(new CardSerializer<CMCard>()) //
				.build() //
				.resolve();
		return cardsWithForeingReferences;
	}

	public Iterable<Card> resolveCardForeignReferences(final CMClass fetchedClass,
			final Iterable<CMCard> fetchedCards) {
		final Iterable<CMCard> cardsWithForeingReferences = resolveCMCardForeignReferences(fetchedClass, fetchedCards);
		return from(cardsWithForeingReferences) //
				.transform(CMCARD_TO_CARD);
	}

	private PagedElements<Card> fetchCardsWithoutClassName(final QueryOptions queryOptions) {
		final PagedElements<CMCard> fetchedCards = DataViewCardFetcher.newInstance() //
				.withDataView(dataView) //
				.withQueryOptions(queryOptions) //
				.build() //
				.fetch();

		final Iterable<CMCard> cardsWithForeingReferences = ForeignReferenceResolver.<CMCard> newInstance() //
				.withEntries(fetchedCards) //
				.withEntryFiller(CardEntryFiller.newInstance() //
						.build()) //
				.withSerializer(new CardSerializer<CMCard>()) //
				.build() //
				.resolve();

		final Iterable<Card> cards = from(cardsWithForeingReferences) //
				.transform(CMCARD_TO_CARD);

		return new PagedElements<Card>(cards, fetchedCards.totalSize());
	}

	public static final Map<String, Object> NO_PARAMETERS = emptyMap();

	/**
	 * Execute a given SQL function to select a set of rows Return these rows as
	 * fake cards
	 *
	 * @param functionName
	 * @param queryOptions
	 * @return
	 */
	@Override
	public PagedElements<Card> fetchSQLCards(final String functionName, final QueryOptions queryOptions) {
		final CMFunction fetchedFunction = dataView.findFunctionByName(functionName);
		if (fetchedFunction == null) {
			final List<Card> emptyCardList = emptyList();
			return new PagedElements<Card>(emptyCardList, 0);
		}
		final Map<String, Object> parameters = ChainablePutMap
				.of(newHashMap(defaultIfNull(queryOptions.getParameters(), NO_PARAMETERS))) //
				.chainablePut(USER, operationUser.getAuthenticatedUser().getId()) //
				.chainablePut(ROLE, operationUser.getPreferredGroup().getId());
		final Alias functionAlias = name("f");
		final CMQueryResult queryResult = new DataViewCardFetcher.SqlQuerySpecsBuilderBuilder() //
				.withDataView(dataView) //
				.withSystemDataView(systemDataView) //
				.withQueryOptions(queryOptions) //
				.withFunction(call(fetchedFunction, parameters)) //
				.withAlias(functionAlias) //
				.build() //
				.count() //
				.run();
		final Iterable<Card> filteredCards = from(queryResult) //
				.transform(toValueSet(functionAlias)) //
				.transform(new Function<CMValueSet, Card>() {

					@Override
					public Card apply(final CMValueSet input) {
						return Card.newInstance() //
								.withClassName(functionName) //
								.withAllAttributes(input.getValues()) //
								.build();
					}

				});
		return new PagedElements<Card>(filteredCards, queryResult.totalSize());
	}

	/**
	 *
	 * @param className
	 * @param cardId
	 * @param queryOptions
	 * @return a long (zero based) with the position of this card in relation of
	 *         current sorting and filter
	 */
	@Override
	public CMCardWithPosition getCardPosition(final String className, final Long cardId,
			final QueryOptions queryOptions) {
		try {
			final PagedElements<CMCardWithPosition> cards = fetchCardsWithPosition(className, queryOptions, cardId);
			return cards.iterator().next();
		} catch (final Exception ex) {
			Log.CMDBUILD.error("Cannot calculate the position for card with id " + cardId + " from class " + className);
			return new CMCardWithPosition(null, -1L);
		}
	}

	@Override
	public PagedElements<CMCardWithPosition> fetchCardsWithPosition(final String className,
			final QueryOptions queryOptions, final Long cardId) {
		final CMClass fetchedClass = dataView.findClass(className);
		final PagedElements<CMQueryRow> rows = DataViewCardFetcher.newInstance() //
				.withClassName(className) //
				.withQueryOptions(queryOptions) //
				.withDataView(dataView) //
				.build() //
				.fetchNumbered(condition(attribute(fetchedClass, ID_ATTRIBUTE), eq(cardId)));
		return new PagedElements<CMCardWithPosition>( //
				from(rows) //
						.transform(new Function<CMQueryRow, CMCardWithPosition>() {

							@Override
							public CMCardWithPosition apply(final CMQueryRow input) {
								final CMCard card = input.getCard(fetchedClass);
								final Card _card = from(resolveCardForeignReferences(fetchedClass, asList(card)))
										.get(0);
								return new CMCardWithPosition(_card, input.getNumber() - 1);
							}

						}), //
				rows.totalSize());
	}

	@Override
	@Transactional
	public Long createCard(final Card userGivenCard) {
		return createCard(userGivenCard, true);
	}

	@Override
	@Transactional
	public Long createCard(final Card userGivenCard, final boolean manageAlsoDomainsAttributes) {
		final CMClass entryType = dataView.findClass(userGivenCard.getClassName());
		if (entryType == null) {
			throw NotFoundExceptionType.CLASS_NOTFOUND.createException(userGivenCard.getClassName());
		}

		final Card _userGivenCard = Card.newInstance() //
				.clone(userGivenCard) //
				.withUser(operationUser.getAuthenticatedUser().getUsername()) //
				.build();
		final Store<Card> store = storeOf(_userGivenCard);
		final Storable created = store.create(_userGivenCard);

		if (manageAlsoDomainsAttributes) {
			updateRelationAttributesFromReference( //
					Long.valueOf(created.getIdentifier()), //
					_userGivenCard, //
					_userGivenCard, //
					entryType //
			);
		}

		return Long.valueOf(created.getIdentifier());
	}

	@Override
	public void updateCard(final Card userGivenCard) {
		lockLogic.checkCardLockedbyUser(userGivenCard.getId());
		final Card _userGivenCard = updateCard0(userGivenCard);
		lockLogic.unlockCard(_userGivenCard.getId());
	}

	private Card updateCard0(final Card userGivenCard) {
		final CMClass entryType = dataView.findClass(userGivenCard.getClassName());
		if (entryType == null) {
			throw NotFoundExceptionType.CLASS_NOTFOUND.createException(userGivenCard.getClassName());
		}

		final Card _userGivenCard = Card.newInstance() //
				.clone(userGivenCard) //
				.withUser(operationUser.getAuthenticatedUser().getUsername()) //
				.build();
		final Store<Card> store = storeOf(_userGivenCard);
		final Card currentCard = store.read(_userGivenCard);
		final Card updatedCard = Card.newInstance(entryType) //
				.clone(currentCard) //
				.withAllAttributes(_userGivenCard.getAttributes()) //
				.withUser(_userGivenCard.getUser()) //
				.build();
		store.update(updatedCard);

		/**
		 * fetch card from database (bug #812: if some triggers are executed,
		 * data must be fetched from db)
		 */
		final Card fetchedCard = store.read(storableOf(_userGivenCard.getIdentifier()));

		updateRelationAttributesFromReference(updatedCard.getId(), fetchedCard, _userGivenCard, entryType);
		return _userGivenCard;
	}

	private void updateRelationAttributesFromReference( //
			final Long storedCardId, //
			final Card fetchedCard, //
			final Card userGivenCard, //
			final CMClass entryType //
	) {
		logger.debug("updating relation attributes for references of card '{}'", storedCardId);

		final Map<String, Object> fetchedCardAttributes = fetchedCard.getAttributes();
		final Map<String, Object> userGivenCardAttributes = userGivenCard.getAttributes();

		for (final CMAttribute attribute : from(entryType.getActiveAttributes()) //
				.filter(attributeTypeInstanceOf(ReferenceAttributeType.class))) {
			Long sourceCardId = null;
			Long destinationCardId = null;
			try {
				final String referenceAttributeName = attribute.getName();
				logger.debug("checking attribute '{}'", referenceAttributeName);

				/*
				 * Before save, some trigger can update the card If the
				 * reference attribute value is the same of the one given from
				 * the user update the attributes over the relation, and take
				 * the values to set from the card given by the user
				 */
				if (haveDifferentValues(fetchedCard, userGivenCard, referenceAttributeName)) {
					continue;
				}

				// retrieve the reference value
				final Object referencedCardIdObject = fetchedCardAttributes.get(referenceAttributeName);
				final Long referencedCardId = getReferenceCardIdAsLong(referencedCardIdObject);
				if (referencedCardIdObject == null) {
					continue;
				}

				// retrieve the relation attributes
				final String domainName = ((ReferenceAttributeType) attribute.getType()).getDomainName();
				final CMDomain domain = dataView.findDomain(domainName);
				final Map<String, Object> relationAttributes = Maps.newHashMap();
				for (final CMAttribute domainAttribute : domain.getAttributes()) {
					final String domainAttributeName = format("_%s_%s", referenceAttributeName,
							domainAttribute.getName());
					final Object domainAttributeValue = userGivenCardAttributes.get(domainAttributeName);
					relationAttributes.put(domainAttribute.getName(), domainAttributeValue);
				}

				// update the attributes if needed
				final CMClass sourceClass = domain.getClass1();
				final CMClass destinationClass = domain.getClass2();

				if (domain.getCardinality().equals("N:1")) {
					sourceCardId = storedCardId;
					destinationCardId = referencedCardId;
				} else {
					sourceCardId = referencedCardId;
					destinationCardId = storedCardId;
				}

				if (sourceCardId == null || destinationCardId == null) {
					continue;
				}

				final Alias DOM = name("DOM");
				final Alias DST = name(format("DST-%s-%s", destinationClass.getName(), randomAlphanumeric(10)));
				final CMQueryRow row = dataView.select(anyAttribute(DOM)) //
						.from(sourceClass) //
						.join(destinationClass, DST, over(domain, as(DOM))) //
						.where(and( //
								condition(attribute(sourceClass, ID_ATTRIBUTE), eq(sourceCardId)), //
								condition(attribute(DST, ID_ATTRIBUTE), eq(destinationCardId)) //
				)) //
						.limit(1) //
						.skipDefaultOrdering() //
						.run() //
						.getOnlyRow();
				final CMCard fetchedSourceCard = row.getCard(sourceClass);
				final CMCard fetchedDestinationCard = row.getCard(DST);
				final CMRelation relation = row.getRelation(DOM).getRelation();

				final boolean updateRelationNeeded = areRelationAttributesModified(relation.getValues(),
						relationAttributes, domain);

				if (updateRelationNeeded) {
					final CMRelationDefinition mutableRelation = dataView.update(relation) //
							.setCard1(fetchedSourceCard) //
							.setCard2(fetchedDestinationCard); //
					updateRelationDefinitionAttributes(relationAttributes, mutableRelation);
					mutableRelation.update();
				}

			} catch (final Exception ex) {
				logger.error("error updating attributes", ex);
			}
		}
	}

	private boolean haveDifferentValues( //
			final Card fetchedCard, //
			final Card userGivenCard, //
			final String referenceAttributeName //
	) {

		final Long fetchedCardAttributeValue = getReferenceCardIdAsLong( //
				fetchedCard.getAttribute(referenceAttributeName));

		final Long userGivenCardAttributeValue = getReferenceCardIdAsLong( //
				userGivenCard.getAttribute(referenceAttributeName));

		boolean output;
		if (fetchedCardAttributeValue == null) {
			output = (userGivenCard != null);
		} else {
			output = !fetchedCardAttributeValue.equals(userGivenCardAttributeValue);
		}
		return output;
	}

	private Long getReferenceCardIdAsLong(final Object value) {
		final Long out;
		if (value instanceof Number) {
			out = Number.class.cast(value).longValue();
		} else if (value instanceof IdAndDescription) {
			out = ((IdAndDescription) value).getId();
		} else if (value instanceof String) {
			final String stringCardId = String.class.cast(value);
			if (isEmpty(stringCardId)) {
				out = null;
			} else {
				out = Long.parseLong(stringCardId);
			}
		} else {
			if (value != null) {
				throw new UnsupportedOperationException("A reference could have a CardReference value");
			}
			out = null;
		}
		return out;
	}

	private boolean areRelationAttributesModified(final Iterable<Entry<String, Object>> oldValues,
			final Map<String, Object> newValues, final CMDomain domain) {

		for (final Entry<String, Object> oldEntry : oldValues) {
			final String attributeName = oldEntry.getKey();
			final Object oldAttributeValue = oldEntry.getValue();
			final CMAttributeType<?> attributeType = domain.getAttribute(attributeName).getType();
			final Object newValueConverted = attributeType.convertValue(newValues.get(attributeName));

			/*
			 * Usually null == null is false. But, here we wanna know if the
			 * value is been changed, so if it was null, and now is still null,
			 * the attribute value is not changed. Do you know that the
			 * CardReferences (value of reference and lookup attributes)
			 * sometimes are null and sometimes is a null-object... Cool! isn't
			 * it? So compare them could be a little tricky
			 */
			if (oldAttributeValue == null) {
				if (newValueConverted == null) {
					continue;
				} else {
					return true;
				}
			} else {
				if (!oldAttributeValue.equals(newValueConverted)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void updateCards(final Iterable<Card> cards) {
		for (final Card card : cards) {
			updateCard0(card);
		}
	}

	@Override
	public void updateFetchedCard(final Card card, final Map<String, Object> attributes) {
		if (card != null) {
			final Card updatedCard = Card.newInstance() //
					.clone(card) //
					.clearAttributes() //
					.withAllAttributes(attributes) //
					.withUser(operationUser.getAuthenticatedUser().getUsername()) //
					.build();
			storeOf(updatedCard).update(updatedCard);
		}
	}

	@Override
	@Transactional
	public void deleteCard(final String className, final Long cardId) {
		lockLogic.checkNotLockedCard(cardId);

		final Card card = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();

		try {
			storeOf(card).delete(card);
		} catch (final UncategorizedSQLException e) {
			/*
			 * maybe not the best way to identify the SQL error..
			 */
			final String message = e.getMessage();
			final RuntimeException _e;
			if (message != null && message.contains("ERROR: CM_RESTRICT_VIOLATION")) {
				_e = ConsistencyExceptionType.ORM_CANT_DELETE_CARD_WITH_RELATION.createException();
			} else {
				_e = new RuntimeException("error deleting card", e);
			}
			throw _e;

		}
	}

	@Override
	public Iterable<CMDomain> findDomainsForClass(final String className, final boolean skipDisabledClasses) {
		final CMClass fetchedClass = dataView.findClass(className);
		if (fetchedClass == null) {
			throw NotFoundExceptionType.CLASS_NOTFOUND.createException(className);
		}
		return from(dataView.findDomains()) //
				.filter(domainFor(fetchedClass)) //
				.filter(skipDisabledClasses ? and(activeOnes(), not(disabledClass(fetchedClass))) : allDomains()) //
				.filter(CMDomain.class);
	}

	/**
	 * Tells if the given class is a subclass of Activity
	 *
	 * @return {@code true} if if the given class is a subclass of Activity,
	 *         {@code false} otherwise
	 */
	@Override
	public boolean isProcess(final CMClass target) {
		final CMClass activity = systemDataView.getActivityClass();
		return activity.isAncestorOf(target);
	}

	/**
	 * Relations.... move the following code to another class
	 */

	@Override
	@Transactional
	public Iterable<Long> createRelations(final RelationDTO relationDTO) {
		final CMDomain domain = dataView.findDomain(relationDTO.domainName);
		if (domain == null) {
			throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(relationDTO.domainName);
		}
		final CMCard parentCard = retrieveParentCard(relationDTO);
		final List<CMCard> childCards = retrieveChildCards(relationDTO);

		final List<Long> ids = Lists.newArrayList();
		if (relationDTO.master.equals("_1")) {
			for (final CMCard dstCard : childCards) {
				if (from(domain.getDisabled1()).contains(parentCard)) {
					throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(relationDTO.domainName);
				}
				if (from(domain.getDisabled2()).contains(dstCard)) {
					throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(relationDTO.domainName);
				}
				final Long id = saveRelation(domain, parentCard, dstCard, relationDTO.relationAttributeToValue);
				ids.add(id);
			}
		} else {
			for (final CMCard srcCard : childCards) {
				if (from(domain.getDisabled1()).contains(srcCard)) {
					throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(relationDTO.domainName);
				}
				if (from(domain.getDisabled2()).contains(parentCard)) {
					throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(relationDTO.domainName);
				}
				final Long id = saveRelation(domain, srcCard, parentCard, relationDTO.relationAttributeToValue);
				ids.add(id);
			}
		}
		return ids;
	}

	private CMCard retrieveParentCard(final RelationDTO relationDTO) {
		Map<Long, String> cardToClassName;
		if (relationDTO.master.equals("_1")) {
			cardToClassName = relationDTO.srcCardIdToClassName;
		} else {
			cardToClassName = relationDTO.dstCardIdToClassName;
		}
		for (final Long cardId : cardToClassName.keySet()) {
			final String className = cardToClassName.get(cardId);
			return cardOf(className, cardId);
		}
		return null; // should be unreachable
	}

	private List<CMCard> retrieveChildCards(final RelationDTO relationDTO) {
		final List<CMCard> childCards = Lists.newArrayList();
		Map<Long, String> cardToClassName;
		if (relationDTO.master.equals("_1")) {
			cardToClassName = relationDTO.dstCardIdToClassName;
		} else {
			cardToClassName = relationDTO.srcCardIdToClassName;
		}
		for (final Long cardId : cardToClassName.keySet()) {
			final String className = cardToClassName.get(cardId);
			childCards.add(cardOf(className, cardId));
		}
		return childCards;
	}

	private Long saveRelation(final CMDomain domain, final CMCard srcCard, final CMCard dstCard,
			final Map<String, Object> attributeToValue) {
		final CMRelationDefinition mutableRelation = dataView.createRelationFor(domain);
		mutableRelation.setCard1(srcCard);
		mutableRelation.setCard2(dstCard);
		for (final String attributeName : attributeToValue.keySet()) {
			final Object value = attributeToValue.get(attributeName);
			mutableRelation.set(attributeName, value);
		}
		try {
			mutableRelation.setUser(operationUser.getAuthenticatedUser().getUsername());
			final CMRelation relation = mutableRelation.create();
			return relation.getId();
		} catch (final RuntimeException ex) {
			throw ORMExceptionType.ORM_ERROR_RELATION_CREATE.createException();
		}
	}

	@Override
	@Transactional
	public void updateRelation(final RelationDTO relationDTO) {
		final CMDomain domain = dataView.findDomain(relationDTO.domainName);
		if (domain == null) {
			throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(relationDTO.domainName);
		}

		final Entry<Long, String> srcCard = relationDTO.getUniqueEntryForSourceCard();
		final String srcClassName = srcCard.getValue();
		final Long srcCardId = srcCard.getKey();
		final CMClass srcClass = dataView.findClass(srcClassName);

		final Entry<Long, String> dstCard = relationDTO.getUniqueEntryForDestinationCard();
		final String dstClassName = dstCard.getValue();
		final Long dstCardId = dstCard.getKey();

		final CMCard fetchedDstCard = cardOf(dstClassName, dstCardId);
		final CMCard fetchedSrcCard = cardOf(srcClassName, srcCardId);
		final CMClass dstClass = dataView.findClass(dstClassName);

		CMQueryRow row;
		WhereClause whereCondition;
		CMClass directedSource;

		final Alias destinationAlias = as(DST_ALIAS);
		final Alias domainAlias = as(DOM_ALIAS);

		if (relationDTO.master.equals("_1")) {
			directedSource = srcClass;
			whereCondition = and( //
					condition(attribute(srcClass, ID_ATTRIBUTE), eq(srcCardId)), //
					and( //
							condition(attribute(domainAlias, ID_ATTRIBUTE), eq(relationDTO.relationId)), //
							condition(attribute(domainAlias, "_Src"), eq("_1")) //
			));
		} else {
			directedSource = dstClass;
			whereCondition = and( //
					condition(attribute(dstClass, ID_ATTRIBUTE), eq(dstCardId)), //
					and( //
							condition(attribute(domainAlias, ID_ATTRIBUTE), eq(relationDTO.relationId)), //
							condition(attribute(domainAlias, "_Src"), eq("_2"))));
		}

		row = dataView.select(anyAttribute(directedSource)) //
				.from(directedSource) //
				.join(anyClass(), destinationAlias, over(domain, domainAlias)) //
				.where(whereCondition) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow(); //

		final CMRelation relation = row.getRelation(domainAlias).getRelation();
		final CMRelationDefinition mutableRelation = dataView.update(relation) //
				.setCard1(fetchedSrcCard) //
				.setCard2(fetchedDstCard);

		updateRelationDefinitionAttributes(relationDTO.relationAttributeToValue, mutableRelation);
		mutableRelation.setUser(operationUser.getAuthenticatedUser().getUsername());
		mutableRelation.update();
	}

	private void updateRelationDefinitionAttributes(final Map<String, Object> attributeToValue,
			final CMRelationDefinition relDefinition) {

		for (final Entry<String, Object> entry : attributeToValue.entrySet()) {
			relDefinition.set(entry.getKey(), entry.getValue());
		}
	}

	@Override
	@Transactional
	public void deleteRelation(final String domainName, final Long relationId) {
		final CMDomain domain = dataView.findDomain(domainName);
		if (domain == null) {
			throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(domainName);
		}

		dataView.delete(new IdentifiedRelation(domain, relationId));
	}

	@Override
	public void deleteRelation( //
			final String srcClassName, //
			final Long srcCardId, //
			final String dstClassName, //
			final Long dstCardId, //
			final CMDomain domain) {
		final CMClass sourceClass = dataView.findClass(srcClassName);
		final CMClass destinationClass = dataView.findClass(dstClassName);
		final CMRelation relation = getRelation(srcCardId, dstCardId, domain, sourceClass, destinationClass);
		dataView.delete(relation);
	}

	@Override
	public CMRelation getRelation(final Long srcCardId, final Long dstCardId, final CMDomain domain,
			final CMClass sourceClass, final CMClass destinationClass) {
		/**
		 * The destination alias is mandatory in order to support also
		 * reflective domains
		 */
		final Alias DOM = name("DOM");
		final Alias DST = name(format("DST-%s-%s", destinationClass.getName(), randomAlphanumeric(10)));
		final CMQueryRow row = dataView.select(anyAttribute(sourceClass), anyAttribute(DOM)) //
				.from(sourceClass) //
				.join(destinationClass, DST, over(domain, as(DOM))) //
				.where(and( //
						condition(attribute(sourceClass, ID_ATTRIBUTE), eq(srcCardId)), //
						condition(attribute(DST, ID_ATTRIBUTE), eq(dstCardId)) //
		)) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow();

		final CMRelation relation = row.getRelation(DOM).getRelation();
		return relation;
	}

	@Override
	@Transactional
	public void deleteDetail(final Card master, final Card detail, final String domainName) {
		final CMDomain domain = dataView.findDomain(domainName);
		if (domain == null) {
			throw NotFoundExceptionType.DOMAIN_NOTFOUND.createException(domainName);
		}

		String sourceClassName, destinationClassName;
		Long sourceCardId, destinationCardId;

		if (CARDINALITY_1N.value().equals(domain.getCardinality())) {
			sourceClassName = master.getClassName();
			sourceCardId = master.getId();
			destinationClassName = detail.getClassName();
			destinationCardId = detail.getId();
		} else if (CARDINALITY_N1.value().equals(domain.getCardinality())) {
			sourceClassName = detail.getClassName();
			sourceCardId = detail.getId();
			destinationClassName = master.getClassName();
			destinationCardId = master.getId();
		} else {
			throw new UnsupportedOperationException("You are tring to delete a detail over a N to N domain");
		}

		deleteRelation(sourceClassName, sourceCardId, destinationClassName, destinationCardId, domain);
		deleteCard(detail.getClassName(), detail.getId());
	}

	@Override
	public File exportClassAsCsvFile(final String className, final String separator) {
		final CMClass fetchedClass = dataView.findClass(className);
		final int separatorInt = separator.charAt(0);
		final CsvPreference exportCsvPrefs = new CsvPreference('"', separatorInt, "\n");
		final String fileName = fetchedClass.getIdentifier().getLocalName() + ".csv";
		final String dirName = System.getProperty("java.io.tmpdir");
		final File targetFile = new File(dirName, fileName);
		final DataExporter dataExporter = new CsvExporter(targetFile, exportCsvPrefs);
		final CMDataSource dataSource = new DBDataSource(dataView, fetchedClass);
		return dataExporter.export(dataSource);
	}

	@Override
	public CSVData importCsvFileFor(final DataHandler csvFile, final Long classId, final String separator)
			throws IOException, JSONException {
		final CMClass destinationClassForImport = dataView.findClass(classId);
		final int separatorInt = separator.charAt(0);
		final CsvPreference importCsvPreferences = new CsvPreference('"', separatorInt, "\n");
		final CsvReader csvReader = new SuperCsvCsvReader(importCsvPreferences);
		final CSVImporter csvImporter = new CSVImporter(csvReader, dataView, lookupStore, destinationClassForImport);

		final CSVData csvData = csvImporter.getCsvDataFrom(csvFile);
		return csvData;
	}

	private CMCard cardOf(final String className, final Long cardId) {
		final CMClass entryType = dataView.findClass(className);
		final CMQueryRow row;
		try {
			row = dataView.select(anyAttribute(entryType)) //
					.from(entryType) //
					.where(condition(attribute(entryType, ID_ATTRIBUTE), eq(cardId))) //
					.limit(1) //
					.skipDefaultOrdering() //
					.run() //
					.getOnlyRow();
		} catch (final NoSuchElementException ex) {
			throw NotFoundExceptionType.CARD_NOTFOUND.createException();
		}
		final CMCard card = row.getCard(entryType);
		return card;
	}

}
