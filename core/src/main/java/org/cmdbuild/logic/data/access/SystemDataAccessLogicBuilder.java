package org.cmdbuild.logic.data.access;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.LockLogic;

public class SystemDataAccessLogicBuilder extends DataAccessLogicBuilder {

	public SystemDataAccessLogicBuilder( //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final CMDataView dataView, //
			final OperationUser operationUser, //
			final LockLogic lockLogic //
	) {
		super(systemDataView, lookupStore, dataView, operationUser, lockLogic);
	}

}
