package utils;

import static org.mockito.Mockito.mock;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.lookup.DataViewLookupStore;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.junit.After;
import org.junit.BeforeClass;

/**
 * Class containing methods for initializing the integration tests database
 */
public abstract class IntegrationTestBase {

	protected static final DBInitializer dbInitializer = new DBInitializer();

	private final DBDriver testDriver;
	private final DBDataView dbView;

	protected IntegrationTestBase() {
		this.testDriver = createTestDriver();
		this.dbView = new DBDataView(testDriver);
	}

	/**
	 * Override if you need to decorate the default.
	 */
	protected DBDriver createBaseDriver() {
		return dbInitializer.getDriver();
	}

	/**
	 * Override if you don't need/want the rollback driver.
	 */
	protected DBDriver createTestDriver() {
		return new GenericRollbackDriver(createBaseDriver());
	}

	public DBDriver dbDriver() {
		return testDriver;
	}

	public DBDataView dbDataView() {
		return dbView;
	}

	public OperationUser operationUser() {
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		final PrivilegeContext privilegeContext = new SystemPrivilegeContext();
		final CMGroup group = mock(CMGroup.class);
		return new OperationUser(authenticatedUser, privilegeContext, group);
	}

	public LookupStore lookupStore() {
		final DataViewStore<Lookup> store = DataViewStore.newInstance(dbView, new LookupStorableConverter());
		return new DataViewLookupStore(store);
	}

	@BeforeClass
	public static void initialize() {
		dbInitializer.initialize();
	}

	@After
	public final void rollback() {
		if (testDriver instanceof GenericRollbackDriver) {
			GenericRollbackDriver.class.cast(testDriver).rollback();
		}
	}

}
