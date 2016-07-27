package org.cmdbuild.service.rest.v1.cxf.service;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.v1.model.Session;

import com.google.common.base.Optional;

public interface OperationUserStore {

	interface BySession {

		void main(OperationUser value);

		void impersonate(OperationUser operationUser);

		Optional<OperationUser> get();

	}

	BySession of(Session value);

	void remove(Session key);

}
