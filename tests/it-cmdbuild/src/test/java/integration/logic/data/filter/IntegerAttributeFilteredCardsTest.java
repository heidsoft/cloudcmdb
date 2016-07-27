package integration.logic.data.filter;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;

import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.cmdbuild.model.data.Card;
import org.json.JSONObject;
import org.junit.Test;

/**
 * In this class there are tests that filter cards for the attribute with type
 * integer.
 */
public class IntegerAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String INTEGER_ATTRIBUTE = "attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(INTEGER_ATTRIBUTE, new IntegerAttributeType(),
				createdClass);

		dbDataView().createCardFor(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), Integer.valueOf(1)) //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), Integer.valueOf(2)) //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), Integer.valueOf(3)) //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("zzz") //
				.setDescription("desc_zzz") //
				.set(createdAttribute.getName(), Integer.valueOf(4)) //
				.save();
	}

	@Test
	public void fetchFilteredCardsWithEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.EQUAL,
				Integer.valueOf(2));
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
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.NOT_EQUAL,
				Integer.valueOf(2));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(3, size(fetchedCards));
		assertEquals("baz", get(fetchedCards, 0).getAttribute("Code"));
		assertEquals("foo", get(fetchedCards, 1).getAttribute("Code"));
		assertEquals("zzz", get(fetchedCards, 2).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithGreaterThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.GREATER_THAN,
				Integer.valueOf(2));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(2, size(fetchedCards));
		assertEquals("baz", get(fetchedCards, 0).getAttribute("Code"));
		assertEquals("zzz", get(fetchedCards, 1).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithLessThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.LESS_THAN,
				Integer.valueOf(2));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(1, size(fetchedCards));
		assertEquals("foo", get(fetchedCards, 0).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithBetweenOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.BETWEEN,
				Integer.valueOf(2), Integer.valueOf(4));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(3, size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		dbDataView().createCardFor(createdClass) //
				.setCode("code_of_null_card") //
				.setDescription("desc_of_null_card") //
				.set(INTEGER_ATTRIBUTE, null) //
				.save();
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(1, size(fetchedCards));
		assertEquals("code_of_null_card", get(fetchedCards, 0).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithNotNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(INTEGER_ATTRIBUTE, FilterOperator.NOT_NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(4, size(fetchedCards));
	}

}
