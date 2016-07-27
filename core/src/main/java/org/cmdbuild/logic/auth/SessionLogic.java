package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.user.OperationUser;

public interface SessionLogic extends AuthenticationLogic {

	interface Callback {

		void sessionCreated(String id);

	}

	String create(LoginDTO login);

	ClientAuthenticationResponse create(ClientRequest request, Callback callback);

	boolean exists(String id);

	void update(String id, LoginDTO login);

	void delete(String id);

	void impersonate(String id, String username);

	String getCurrent();

	void setCurrent(String id);

	boolean isValidUser(String id);

	OperationUser getUser(String id);

}
