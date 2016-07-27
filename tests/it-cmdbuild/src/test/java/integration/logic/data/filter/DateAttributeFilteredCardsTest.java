package integration.logic.data.filter;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;

import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.cmdbuild.model.data.Card;
import org.json.JSONObject;
import org.junit.Test;

/**
 * In this class there are tests that filter cards for the attribute with type
 * date.
 */
public class DateAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String DATE_ATTRIBUTE = "Attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(DATE_ATTRIBUTE, new DateAttributeType(), createdClass);

		dbDataView().createCardFor(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), "06/08/2012") //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), "11/12/1995") //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), "10/09/1998") //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("zzz") //
				.setDescription("desc_zzz") //
				.set(createdAttribute.getName(), null) //
				.save();
	}

	@Test
	public void fetchFilteredCardsWithEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.EQUAL, "11/12/1995");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(1, size(fetchedCards));
		assertEquals("bar", get(fetchedCards, 0).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithNotEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.NOT_EQUAL, "11/12/1995");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(3, size(fetchedCards));
		// sorted by description ascending by default
		assertEquals("baz", get(fetchedCards, 0).getAttribute("Code"));
		assertEquals("foo", get(fetchedCards, 1).getAttribute("Code"));
		assertEquals("zzz", get(fetchedCards, 2).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithGreaterThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.GREATER_THAN, "10/09/1998");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(1, size(fetchedCards));
		assertEquals("foo", get(fetchedCards, 0).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithLessThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.LESS_THAN, "11/09/1998");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(2, size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithBetweenOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.BETWEEN, "19/01/1990",
				"19/01/2000");
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(2, size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(1, size(fetchedCards));
		assertEquals("zzz", get(fetchedCards, 0).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithNotNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DATE_ATTRIBUTE, FilterOperator.NOT_NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(3, size(fetchedCards));
	}

}
