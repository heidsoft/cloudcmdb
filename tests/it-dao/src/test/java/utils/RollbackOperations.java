package utils;

import org.cmdbuild.dao.driver.DBDriver;
import org.junit.rules.ExternalResource;

public class RollbackOperations extends ExternalResource {

	public static interface Context {

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

	public static class Builder implements org.cmdbuild.common.Builder<RollbackOperations> {

		private DBDriver dbDriver;
		private Hook hook = NULL_HOOK;

		@Override
		public RollbackOperations build() {
			return new RollbackOperations(this);
		}

		public Builder forDriver(final DBDriver dbDriver) {
			this.dbDriver = dbDriver;
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

	private final DBDriver dbDriver;
	private final Hook hook;

	private RollbackOperations(final Builder builder) {
		this.dbDriver = builder.dbDriver;
		this.hook = builder.hook;
	}

	@Override
	protected void before() throws Throwable {
		super.before();
		hook.before(context());
	}

	@Override
	protected void after() {
		super.after();
		hook.after(context());
		if (dbDriver instanceof GenericRollbackDriver) {
			GenericRollbackDriver.class.cast(dbDriver).rollback();
		}
	}

	private Context context() {
		return new Context() {

		};
	}

}
