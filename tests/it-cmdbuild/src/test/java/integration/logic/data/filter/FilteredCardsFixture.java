package integration.logic.data.filter;

import static integration.logic.data.DataDefinitionLogicTest.a;
import static integration.logic.data.DataDefinitionLogicTest.newClass;
import static utils.IntegrationTestUtils.newAttribute;

import java.util.Map;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.lookup.DataViewLookupStore;
import org.cmdbuild.data.store.lookup.LookupStorableConverter;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic;
import org.cmdbuild.logic.data.DummyLockLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;

import utils.GenericRollbackDriver;
import utils.IntegrationTestBase;

import com.google.common.collect.Lists;

public abstract class FilteredCardsFixture extends IntegrationTestBase {

	private static final String CLASS_NAME = "test_class";

	private DataDefinitionLogic dataDefinitionLogic;
	protected DataAccessLogic dataAccessLogic;
	protected CMClass createdClass;

	/**
	 * Actually, {@link GenericRollbackDriver} cannot clear attribute data
	 * before trying to delete the attribute itself. So we must manually clean
	 * all class data just to be sure to have rollback working well without
	 * errors.
	 */
	@Override
	protected DBDriver createTestDriver() {
		return createBaseDriver();
	}

	@Before
	public void setUp() throws Exception {
		dataDefinitionLogic = new DefaultDataDefinitionLogic(dbDataView());
		dataAccessLogic = new UserDataAccessLogicBuilder( //
				dbDataView(), //
				new DataViewLookupStore( //
						DataViewStore.newInstance(dbDataView(), new LookupStorableConverter())), //
				dbDataView(), //
				operationUser(), //
				new DummyLockLogic()) //
				.build();
		createClassesAndDomains();
		initializeDatabaseData();
	}

	/**
	 * It can be overridden by classes that extends this fixture in order to
	 * initialize the database with other classes and domains.
	 */
	protected void createClassesAndDomains() {
		createdClass = dataDefinitionLogic().createOrUpdate(a(newClass(CLASS_NAME) //
				.thatIsActive(true)));
	}

	/**
	 * It must be overridden by classes that extends this fixture in order to
	 * initialize the database with known attributes and cards.
	 */
	protected abstract void initializeDatabaseData();

	@After
	public void tearDown() {
		clearAndDeleteClassesAndDomains();
	}

	/**
	 * It can be overridden by classes that extends this fixture in order to
	 * clean the database from classes and domains created using
	 * {@link #createClassesAndDomains()}.
	 * 
	 * Don't use business logic inside this method, we want to be sure that
	 * classes and domains are really deleted or have an exception that show us
	 * that something went wrong
	 */
	protected void clearAndDeleteClassesAndDomains() {
		dbDataView().clear(createdClass);
		dbDataView().delete(createdClass);
	}

	protected DataDefinitionLogic dataDefinitionLogic() {
		return dataDefinitionLogic;
	}

	protected QueryOptions createQueryOptions(final int limit, final int offset, final JSONArray sorters,
			final JSONObject filter) {
		return QueryOptions.newQueryOption() //
				.limit(limit) //
				.offset(offset) //
				.orderBy(sorters) //
				.filter(filter) //
				.build();
	}

	protected DBAttribute addAttributeToClass(final String name, final CMAttributeType<?> type, final CMClass klass) {
		return dbDataView().createAttribute(newAttribute(name, type, dbDataView().findClass(klass.getName())));
	}

	protected DBAttribute addAttributeToClass(final String name, final CMAttributeType<?> type, final DBClass klass) {
		return dbDataView().createAttribute(newAttribute(name, type, klass));
	}

	protected void insertCardWithValues(final DBClass klass, final Map<String, Object> attributeNameToValue) {
		final DBCard cardToBeCreated = dbDataView().createCardFor(klass);
		for (final String key : attributeNameToValue.keySet()) {
			cardToBeCreated.set(key, attributeNameToValue.get(key));
		}
		cardToBeCreated.save();
	}

	protected JSONObject buildAttributeFilter(final String attributeName, final FilterOperator operator,
			final Object... values) throws JSONException {
		String valuesString = "";
		final Object[] valuesArray = Lists.newArrayList(values).toArray();
		for (int i = 0; i < valuesArray.length; i++) {
			valuesString = valuesString + quoteIfNecessary(valuesArray[i].toString());
			if (i < valuesArray.length - 1) {
				valuesString = valuesString + ",";
			}
		}

		final String s = "{attribute: {simple: {attribute: " + attributeName + ", operator: " + operator.toString()
				+ ", value:[" + valuesString + "]}}}";
		return new JSONObject(s);
	}

	private String quoteIfNecessary(final String notQuotedString) {
		if (notQuotedString.contains("/")) {
			return JSONObject.quote(notQuotedString);
		}
		return notQuotedString;
	}

}
