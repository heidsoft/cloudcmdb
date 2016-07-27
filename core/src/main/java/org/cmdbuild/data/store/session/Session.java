package org.cmdbuild.data.store.session;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.data.store.Storable;

public interface Session extends Storable {

	OperationUser getUser();

	OperationUser getImpersonated();

}
