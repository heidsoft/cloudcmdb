package stress.logic.data;

import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.junit.Assert.assertEquals;
import static utils.IntegrationTestUtils.newClass;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.lookup.DataViewLookupStore;
import org.cmdbuild.data.store.lookup.LookupStorableConverter;
import org.cmdbuild.logic.data.DummyLockLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.model.data.Card;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

public class QueryStressTest extends IntegrationTestBase {

	private DataAccessLogic dataAccessLogic;
	private DBClass stressTestClass;
	private static final String CLASS_NAME = "stress_test_class";
	private static final int NUMBER_OF_CARDS = 300;

	@Override
	protected DBDriver createTestDriver() {
		return super.createBaseDriver();
	}

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
		final DBDriver pgDriver = dbDriver();
		stressTestClass = pgDriver.findClass(CLASS_NAME);
		if (stressTestClass == null) {
			stressTestClass = dbDataView().create(newClass(CLASS_NAME, null));
		}
		storeBigAmountOfCardsIfNeeded();
	}

	@Test(timeout = 200)
	public void evaluatePaginationPerformanceInQueries() throws Exception {
		// given
		final QueryOptions queryOptions = createQueryOptions(100, 0, null, null);

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions).elements();

		// then
		assertEquals(size(cards), 100);
	}

	@Test(timeout = 200)
	public void evaluatePaginationPerformanceWithSortingAndFilters() throws Exception {
		// given
		final JSONArray sortersArray = new JSONArray();
		sortersArray.put(new JSONObject("{property: Code, direction: ASC}"));
		final JSONObject filter = new JSONObject(
				"{attribute: {simple: {attribute: Code, operator: equal, value: ['100']}}}");
		final QueryOptions queryOptions = createQueryOptions(150, 0, sortersArray, filter);

		// when
		final Iterable<Card> cards = dataAccessLogic.fetchCards(CLASS_NAME, queryOptions).elements();

		// then
		assertEquals(size(cards), 1);
	}

	private void storeBigAmountOfCardsIfNeeded() {
		final CMQueryResult result = dbDataView() //
				.select(anyAttribute(stressTestClass)) //
				.from(stressTestClass) //
				.count() //
				.run();
		if (result.totalSize() < NUMBER_OF_CARDS) {
			for (int i = result.totalSize(); i < NUMBER_OF_CARDS; i++) {
				dbDataView().createCardFor(stressTestClass) //
						.setCode("" + i) //
						.setDescription("desc_" + i) //
						.save();
			}
		}
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

}
