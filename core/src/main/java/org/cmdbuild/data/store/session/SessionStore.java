package org.cmdbuild.data.store.session;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.store.Store;

public interface SessionStore extends Store<Session> {

	OperationUser selectUserOrImpersonated(Session session);

}
