package integration.logic.data.filter;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;

import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.cmdbuild.model.data.Card;
import org.json.JSONObject;
import org.junit.Test;

/**
 * In this class there are tests that filter cards for the attribute with type
 * integer.
 */
public class DoubleAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String DOUBLE_ATTRIBUTE = "attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(DOUBLE_ATTRIBUTE, new DoubleAttributeType(),
				createdClass);

		dbDataView().createCardFor(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), Double.valueOf(1)) //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), Double.valueOf(2.4323)) //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), Double.valueOf(-50.32129559)) //
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
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.EQUAL,
				Double.valueOf(2.4323));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(1, size(fetchedCards));
		assertEquals("bar", get(fetchedCards, 0).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithNotEqualOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.NOT_EQUAL,
				Double.valueOf(2.4323));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(3, size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithGreaterThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.GREATER_THAN,
				Double.valueOf(-100));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(3, size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithLessThanOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.LESS_THAN,
				Double.valueOf(1.8));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(2, size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithBetweenOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.BETWEEN,
				Double.valueOf(-10.34), Double.valueOf(4));
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(2, size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(1, size(fetchedCards));
	}

	@Test
	public void fetchFilteredCardsWithNotNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(DOUBLE_ATTRIBUTE, FilterOperator.NOT_NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getIdentifier().getLocalName(),
				queryOptions).elements();

		// then
		assertEquals(3, size(fetchedCards));
	}

}
