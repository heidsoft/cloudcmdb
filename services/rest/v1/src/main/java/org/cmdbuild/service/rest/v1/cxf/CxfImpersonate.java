package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.base.Predicates.not;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.service.rest.v1.Impersonate;

import com.google.common.base.Predicate;

public class CxfImpersonate implements Impersonate {

	private final ErrorHandler errorHandler;
	private final SessionLogic sessionLogic;
	private final Predicate<OperationUser> operationUserAllowed;

	public CxfImpersonate(final ErrorHandler errorHandler, final SessionLogic sessionLogic,
			final Predicate<OperationUser> operationUserAllowed) {
		this.errorHandler = errorHandler;
		this.sessionLogic = sessionLogic;
		this.operationUserAllowed = operationUserAllowed;
	}

	@Override
	public void start(final String id, final String username) {
		if (!sessionLogic.exists(id)) {
			errorHandler.sessionNotFound(id);
		}
		final OperationUser current = sessionLogic.getUser(id);
		if (not(operationUserAllowed).apply((current))) {
			errorHandler.notAuthorized();
		}
		sessionLogic.impersonate(id, username);
	}

	@Override
	public void stop(final String id) {
		if (!sessionLogic.exists(id)) {
			errorHandler.sessionNotFound(id);
		}
		sessionLogic.impersonate(id, null);
	}

}
