package integration.logic.data;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utils.IntegrationTestUtils.newClass;
import static utils.IntegrationTestUtils.newDomain;
import static utils.IntegrationTestUtils.withIdentifier;

import java.math.BigDecimal;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.lookup.DataViewLookupStore;
import org.cmdbuild.data.store.lookup.LookupStorableConverter;
import org.cmdbuild.logic.commands.GetRelationList.DomainWithSource;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic;
import org.cmdbuild.logic.data.DummyLockLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Card;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

import com.google.common.collect.Iterables;

public class DataAccessLogicTest extends IntegrationTestBase {

	private static final String INTEGER_ATTRIBUTE_NAME = "integer_attr";
	private static final String DOUBLE_ATTRIBUTE_NAME = "double_attr";
	private static final String DECIMAL_ATTRIBUTE_NAME = "decimal_attr";
	private static final String STRING_ATTRIBUTE_NAME = "string_attr";
	private static final String CHAR_ATTRIBUTE_NAME = "char_attr";
	private static final String TEXT_ATTRIBUTE_NAME = "text_attr";
	private static final String INET_ATTRIBUTE_NAME = "inet_attr";
	private static final String DATE_ATTRIBUTE_NAME = "date_attr";
	private static final String TIME_ATTRIBUTE_NAME = "time_attr";
	private static final String TIMESTAMP_ATTRIBUTE_NAME = "timestamp_attr";
	private static final String BOOLEAN_ATTRIBUTE_NAME = "boolean_attr";

	private DataAccessLogic dataAccessLogic;

	@Before
	public void createDataDefinitionLogic() throws Exception {
		dataAccessLogic = new UserDataAccessLogicBuilder( //
				dbDataView(), //
				new DataViewLookupStore( //
						DataViewStore.newInstance(dbDataView(), new LookupStorableConverter())), //
				dbDataView(), //
				operationUser(), //
				new DummyLockLogic()) //
				.build();
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionIfNullClassName() throws Exception {
		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), new JSONObject());
		dataAccessLogic.fetchCards(null, queryOptions);
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotRetrieveCardsIfNotExistentClassName() throws Exception {
		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), new JSONObject());
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards("not_existent_class_name", queryOptions)
				.elements();

		// then
		assertTrue(isEmpty(fetchedCards));
	}

	@Test
	public void shouldRetrieveAllCardsIfFilterIsNull() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), null);
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(size(fetchedCards), 3);
	}

	@Test
	public void shouldRetrieveAllCardsIfFilterIsEmpty() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), new JSONObject());
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(size(fetchedCards), 3);
	}

	@Test
	public void shouldSortSuccessfullyFetchedCards() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONArray sortersArray = new JSONArray();
		sortersArray.put(new JSONObject("{property: Code, direction: ASC}"));

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, sortersArray, null);
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(get(fetchedCards, 0).getAttribute("Code"), "bar");
		assertEquals(get(fetchedCards, 1).getAttribute("Code"), "baz");
		assertEquals(get(fetchedCards, 2).getAttribute("Code"), "foo");
		assertEquals(get(fetchedCards, 3).getAttribute("Code"), "zzz");
	}

	@Test
	public void shouldFetchCardsWithFilterDefinedButFullTextQueryEmpty() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{attribute: {simple: {attribute: Code, operator: equal, value:[foo]}}}");

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), filterObject);
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(size(fetchedCards), 1);
		assertEquals(get(fetchedCards, 0).getAttribute("Code"), "foo");
	}

	@Test
	public void shouldFetchCardsWithFilterDefinedAndFullTextQuery() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bar") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_baz") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("zzz") //
				.setDescription("description_baz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{query: dESc, attribute: {simple: {attribute: Code, operator: equal, value:['foo']}}}");

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, new JSONArray(), filterObject);
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(size(fetchedCards), 1);
		assertEquals(get(fetchedCards, 0).getAttribute("Code"), "foo");
	}

	@Test
	public void shouldFetchCardsWithFullTextQueryFilterAndMultipleAttributeSorting() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();
		final JSONObject filterObject = new JSONObject("{query: CRiptioN}");
		final JSONArray sortersArray = new JSONArray();
		sortersArray.put(new JSONObject("{property: Code, direction: DESC}"));
		sortersArray.put(new JSONObject("{property: Description, direction: ASC}"));

		// when
		final QueryOptions queryOptions = createQueryOptions(10, 0, sortersArray, filterObject);
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(size(fetchedCards), 3);
		assertEquals(get(fetchedCards, 0).getAttribute("Code"), "baz");
		assertEquals(get(fetchedCards, 1).getAttribute("Code"), "bar");
		assertEquals(get(fetchedCards, 1).getAttribute("Description"), "description_aaa");
		assertEquals(get(fetchedCards, 2).getAttribute("Code"), "bar");
		assertEquals(get(fetchedCards, 2).getAttribute("Description"), "description_bbb");
	}

	@Test
	public void shouldPaginateSuccessfully() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();

		// when
		final Iterable<Card> firstPageOfCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				createQueryOptions(3, 0, null, null)).elements();
		final Iterable<Card> secondPageOfCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				createQueryOptions(3, 3, null, null)).elements();

		// then
		assertEquals(size(firstPageOfCards), 3);
		assertEquals(size(secondPageOfCards), 1);
	}

	@Test
	public void shouldFetchCardsWithAndConditionsInFilter() throws Exception {
		// given
		final DBClass newClass = dbDataView().create(newClass("test"));

		dbDataView().createCardFor(newClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_aaa") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("bar") //
				.setDescription("description_bbb") //
				.save();
		dbDataView().createCardFor(newClass) //
				.setCode("baz") //
				.setDescription("description_zzz") //
				.save();
		final JSONObject filterObject = new JSONObject(
				"{attribute: {and: [{simple: {attribute: Code, operator: notcontain, value:['bar']}}, "
						+ "{simple: {attribute: Description, operator: contain, value: ['sc_f']}}]}}");

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(newClass.getIdentifier().getLocalName(),
				createQueryOptions(10, 0, null, filterObject)).elements();

		// then
		assertEquals(size(fetchedCards), 1);
		assertEquals(get(fetchedCards, 0).getAttribute("Code"), "foo");
	}

	private QueryOptions createQueryOptions(final int limit, final int offset, final JSONArray sorters,
			final JSONObject filter) {
		return QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.build();
	}

	@Test
	public void shouldFetchAllCardsRelatedToASpecifiedCard() throws Exception {
		// given
		final DBClass srcClass = dbDataView().create(newClass("src"));
		final DBClass dstClass = dbDataView().create(newClass("dst"));
		final DBDomain dom = dbDataView().create(newDomain(withIdentifier("dom"), srcClass, dstClass));
		final CMCard srcCard = dbDataView().createCardFor(srcClass) //
				.setCode("src1") //
				.save();
		dbDataView().createCardFor(srcClass) //
				.setCode("src2") //
				.save();
		for (int i = 0; i < 10; i++) {
			final CMCard dstCard = dbDataView().createCardFor(dstClass) //
					.setCode("dst" + i) //
					.save();
			dbDataView().createRelationFor(dom).setCard1(srcCard).setCard2(dstCard).save();
		}
		final Card card = Card.newInstance() //
				.withClassName(srcClass.getIdentifier().getLocalName()) //
				.withId(srcCard.getId()) //
				.build();
		final DomainWithSource domWithSource = DomainWithSource.create(dom.getId(), "_1");

		// when
		final GetRelationListResponse response1 = dataAccessLogic.getRelationList(card, domWithSource);

		final QueryOptions options2 = QueryOptions.newQueryOption().limit(5).build();
		final GetRelationListResponse response2 = dataAccessLogic.getRelationList(card, domWithSource, options2);

		final QueryOptions options3 = QueryOptions.newQueryOption().limit(5)
				.filter(new JSONObject("{query: no_card_match_this_filter}")).build();
		final GetRelationListResponse response3 = dataAccessLogic.getRelationList(card, domWithSource, options3);

		// then
		assertEquals(10, response1.getTotalNumberOfRelations());
		assertEquals(5, Iterables.size(response2.iterator().next()));
		assertFalse(response3.iterator().hasNext());

	}

	@Test
	public void returnFalseAskingIfAClassIsAProcess() {
		final CMClass fooClass = dbDataView().create(newClass("Foo"));
		assertFalse(dataAccessLogic.isProcess(fooClass));
	}

	@Test
	public void returnTrueAskingIfAProcessIsAProcess() {
		final DBClass activity = dbDataView().findClass("Activity");
		final DBClass fooClass = dbDataView().create(newClass("Foo", activity));
		assertTrue(dataAccessLogic.isProcess(fooClass));
	}

	@Test
	public void notNullValuesInsertedCreatingCardAreEqualsToReadValues() {
		// given
		final CMClass createdClass = createClassWithAllTypeOfAttributes();

		// when
		final Long createdCardId = dbDataView().createCardFor(createdClass) //
				.setCode("code") //
				.setDescription("description") //
				.set(INTEGER_ATTRIBUTE_NAME, 10) //
				.set(DOUBLE_ATTRIBUTE_NAME, 10.8) //
				.set(DECIMAL_ATTRIBUTE_NAME, 10.35) //
				.set(STRING_ATTRIBUTE_NAME, "stringa") //
				.set(CHAR_ATTRIBUTE_NAME, "c") //
				.set(TEXT_ATTRIBUTE_NAME, "text test") //
				.set(DATE_ATTRIBUTE_NAME, "10/02/2012") //
				.set(TIME_ATTRIBUTE_NAME, "18:22:11") //
				.set(TIMESTAMP_ATTRIBUTE_NAME, "10/02/2012 18:22:11") //
				.set(INET_ATTRIBUTE_NAME, "192.168.0.1") //
				.set(BOOLEAN_ATTRIBUTE_NAME, true) //
				.save().getId();
		final Card fetchedCard = dataAccessLogic.fetchCard(createdClass.getName(), createdCardId);

		// then
		assertEquals(10, fetchedCard.getAttribute(INTEGER_ATTRIBUTE_NAME));
		assertEquals(10.8, fetchedCard.getAttribute(DOUBLE_ATTRIBUTE_NAME));
		assertEquals(BigDecimal.valueOf(10.35), fetchedCard.getAttribute(DECIMAL_ATTRIBUTE_NAME));
		assertEquals("stringa", fetchedCard.getAttribute(STRING_ATTRIBUTE_NAME));
		assertEquals("c", fetchedCard.getAttribute(CHAR_ATTRIBUTE_NAME));
		assertEquals("text test", fetchedCard.getAttribute(TEXT_ATTRIBUTE_NAME));
		assertEquals("10/02/2012", fetchedCard.getAttribute(DATE_ATTRIBUTE_NAME));
		assertEquals("18:22:11", fetchedCard.getAttribute(TIME_ATTRIBUTE_NAME));
		assertEquals("10/02/2012 18:22:11", fetchedCard.getAttribute(TIMESTAMP_ATTRIBUTE_NAME));
		assertEquals("192.168.0.1", fetchedCard.getAttribute(INET_ATTRIBUTE_NAME));
		assertEquals(true, fetchedCard.getAttribute(BOOLEAN_ATTRIBUTE_NAME));
	}

	private CMClass createClassWithAllTypeOfAttributes() {
		final CMClass fooClass = dbDataView().create(newClass("Foo"));
		final DataDefinitionLogic dataDefinitionLogic = new DefaultDataDefinitionLogic(new DBDataView(
				createBaseDriver()));

		final Attribute integerAttribute = Attribute.newAttribute() //
				.withName(INTEGER_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("INTEGER").build();
		final Attribute doubleAttribute = Attribute.newAttribute() //
				.withName(DOUBLE_ATTRIBUTE_NAME).withOwnerName(fooClass.getName()) //
				.withType("DOUBLE").build();
		final Attribute decimalAttribute = Attribute.newAttribute() //
				.withName(DECIMAL_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("DECIMAL") //
				.withPrecision(5) //
				.withScale(2).build();
		final Attribute stringAttribute = Attribute.newAttribute() //
				.withName(STRING_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("STRING") //
				.withLength(30).build();
		final Attribute charAttribute = Attribute.newAttribute() //
				.withName(CHAR_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("CHAR").build();
		final Attribute textAttribute = Attribute.newAttribute() //
				.withName(TEXT_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("TEXT").build();
		final Attribute dateAttribute = Attribute.newAttribute() //
				.withName(DATE_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("DATE").build();
		final Attribute timeAttribute = Attribute.newAttribute() //
				.withName(TIME_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("TIME").build();
		final Attribute timestampAttribute = Attribute.newAttribute() //
				.withName(TIMESTAMP_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("TIMESTAMP").build();
		final Attribute inetAttribute = Attribute.newAttribute() //
				.withName(INET_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("INET").build();
		final Attribute booleanAttribute = Attribute.newAttribute() //
				.withName(BOOLEAN_ATTRIBUTE_NAME) //
				.withOwnerName(fooClass.getName()) //
				.withType("BOOLEAN").build();

		dataDefinitionLogic.createOrUpdate(integerAttribute);
		dataDefinitionLogic.createOrUpdate(doubleAttribute);
		dataDefinitionLogic.createOrUpdate(decimalAttribute);
		dataDefinitionLogic.createOrUpdate(stringAttribute);
		dataDefinitionLogic.createOrUpdate(charAttribute);
		dataDefinitionLogic.createOrUpdate(textAttribute);
		dataDefinitionLogic.createOrUpdate(dateAttribute);
		dataDefinitionLogic.createOrUpdate(timeAttribute);
		dataDefinitionLogic.createOrUpdate(timestampAttribute);
		dataDefinitionLogic.createOrUpdate(inetAttribute);
		dataDefinitionLogic.createOrUpdate(booleanAttribute);
		return fooClass;
	}

	@Test
	public void nullValuesInsertedCreatingCardAreEqualsToReadValues() {
		// given
		final CMClass createdClass = createClassWithAllTypeOfAttributes();

		// when
		final Long createdCardId = dbDataView().createCardFor(createdClass) //
				.setCode("code") //
				.setDescription("") //
				.set(INTEGER_ATTRIBUTE_NAME, null) //
				.set(DOUBLE_ATTRIBUTE_NAME, null) //
				.set(DECIMAL_ATTRIBUTE_NAME, null) //
				.set(STRING_ATTRIBUTE_NAME, null) //
				.set(CHAR_ATTRIBUTE_NAME, null) //
				.set(TEXT_ATTRIBUTE_NAME, null) //
				.set(DATE_ATTRIBUTE_NAME, null) //
				.set(TIME_ATTRIBUTE_NAME, null) //
				.set(TIMESTAMP_ATTRIBUTE_NAME, null) //
				.set(INET_ATTRIBUTE_NAME, null) //
				.set(BOOLEAN_ATTRIBUTE_NAME, null) //
				.save().getId();
		final Card fetchedCard = dataAccessLogic.fetchCard(createdClass.getName(), createdCardId);

		// then
		assertEquals(null, fetchedCard.getAttribute(INTEGER_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(DOUBLE_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(DECIMAL_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(STRING_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(CHAR_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(TEXT_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(DATE_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(TIME_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(TIMESTAMP_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(INET_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute(BOOLEAN_ATTRIBUTE_NAME));
		assertEquals(null, fetchedCard.getAttribute("Description"));
	}
}
