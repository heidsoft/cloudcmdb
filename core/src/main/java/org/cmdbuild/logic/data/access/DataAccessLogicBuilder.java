package org.cmdbuild.logic.data.access;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.LockLogic;

public abstract class DataAccessLogicBuilder implements Builder<DataAccessLogic> {

	private final CMDataView systemDataView;
	private final LookupStore lookupStore;
	private final CMDataView dataView;
	private final OperationUser operationUser;
	private final LockLogic lockLogic;

	protected DataAccessLogicBuilder( //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final CMDataView dataView, //
			final OperationUser operationUser, //
			final LockLogic lockLogic //
	) {
		this.systemDataView = systemDataView;
		this.lookupStore = lookupStore;
		this.dataView = dataView;
		this.operationUser = operationUser;
		this.lockLogic = lockLogic;
	}

	@Override
	public DataAccessLogic build() {
		return new DefaultDataAccessLogic( //
				systemDataView, //
				lookupStore, //
				dataView, //
				operationUser, //
				lockLogic);
	}

}
