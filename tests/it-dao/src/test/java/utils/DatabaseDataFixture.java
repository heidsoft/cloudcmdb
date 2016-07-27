package utils;

import javax.sql.DataSource;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.lookup.DataViewLookupStore;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStorableConverter;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.junit.rules.ExternalResource;

public class DatabaseDataFixture extends ExternalResource {

	public static interface Context {

		DataSource dataSource();

	}

	public static interface Hook {

		void before(Context context);

		void after(Context context);

	}

	public static Hook NULL_HOOK = new Hook() {

		@Override
		public void before(final Context context) {
			// nothing to do
		}

		@Override
		public void after(final Context context) {
			// nothing to do
		}

	};

	public static class Builder implements org.cmdbuild.common.Builder<DatabaseDataFixture> {

		private boolean dropAfter;
		private Hook hook = NULL_HOOK;

		@Override
		public DatabaseDataFixture build() {
			return new DatabaseDataFixture(this);
		}

		public Builder dropAfter(final boolean dropAfter) {
			this.dropAfter = dropAfter;
			return this;
		}

		public Builder hook(final Hook hook) {
			this.hook = hook;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final boolean dropAfter;
	private final Hook hook;

	private DatabaseDataFixture(final Builder builder) {
		this.dropAfter = builder.dropAfter;
		this.hook = builder.hook;
	}

	private final DBInitializer dbInitializer = new DBInitializer();

	@Override
	protected void before() throws Throwable {
		super.before();
		dbInitializer.initialize();
		hook.before(context());
	}

	@Override
	protected void after() {
		super.after();
		hook.after(context());
		if (dropAfter) {
			dbInitializer.drop();
		}
	}

	private Context context() {
		return new Context() {

			@Override
			public DataSource dataSource() {
				return DatabaseDataFixture.this.dataSource();
			}

		};
	}

	public DataSource dataSource() {
		return dbInitializer.dataSource();
	}

	/**
	 * Override if you need to decorate the default.
	 */
	public DBDriver baseDriver() {
		return dbInitializer.getDriver();
	}

	/**
	 * Override if you don't need/want the rollback driver.
	 */
	public DBDriver testDriver() {
		return new GenericRollbackDriver(baseDriver());
	}

	public DBDataView systemDataView() {
		return new DBDataView(testDriver());
	}

	public LookupStore lookupStore() {
		final DataViewStore<Lookup> store = DataViewStore.newInstance(systemDataView(), new LookupStorableConverter());
		return new DataViewLookupStore(store);
	}

}
