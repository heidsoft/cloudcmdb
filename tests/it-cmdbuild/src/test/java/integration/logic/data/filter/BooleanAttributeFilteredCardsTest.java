package integration.logic.data.filter;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.junit.Assert.assertEquals;

import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.cmdbuild.model.data.Card;
import org.json.JSONObject;
import org.junit.Test;

/**
 * In this class there are tests that filter cards for the attribute with type
 * boolean.
 */
public class BooleanAttributeFilteredCardsTest extends FilteredCardsFixture {

	private static final String BOOLEAN_ATTRIBUTE = "attr";

	@Override
	protected void initializeDatabaseData() {
		final DBAttribute createdAttribute = addAttributeToClass(BOOLEAN_ATTRIBUTE, new BooleanAttributeType(),
				createdClass);

		dbDataView().createCardFor(createdClass) //
				.setCode("foo") //
				.setDescription("desc_foo") //
				.set(createdAttribute.getName(), Boolean.FALSE) //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("bar") //
				.setDescription("desc_bar") //
				.set(createdAttribute.getName(), Boolean.TRUE) //
				.save();
		dbDataView().createCardFor(createdClass) //
				.setCode("baz") //
				.setDescription("desc_baz") //
				.set(createdAttribute.getName(), Boolean.TRUE) //
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
		final JSONObject filterObject = buildAttributeFilter(BOOLEAN_ATTRIBUTE, FilterOperator.EQUAL, Boolean.TRUE);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(2, size(fetchedCards));
		assertEquals("bar", get(fetchedCards, 0).getAttribute("Code"));
		assertEquals("baz", get(fetchedCards, 1).getAttribute("Code"));
	}

	@Test
	public void fetchFilteredCardsWithNullOperator() throws Exception {
		// given
		final JSONObject filterObject = buildAttributeFilter(BOOLEAN_ATTRIBUTE, FilterOperator.NULL);
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
		final JSONObject filterObject = buildAttributeFilter(BOOLEAN_ATTRIBUTE, FilterOperator.NOT_NULL);
		final QueryOptions queryOptions = createQueryOptions(10, 0, null, filterObject);

		// when
		final Iterable<Card> fetchedCards = dataAccessLogic.fetchCards(createdClass.getName(), queryOptions).elements();

		// then
		assertEquals(3, size(fetchedCards));
	}

}
