package org.cmdbuild.service.rest.v1.cxf;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.v1.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.v1.model.Models.newSession;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.service.rest.v1.Sessions;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.cmdbuild.service.rest.v1.model.Session;

public class CxfSessions implements Sessions, LoggingSupport {

	private final ErrorHandler errorHandler;
	private final SessionLogic sessionLogic;

	public CxfSessions(final ErrorHandler errorHandler, final SessionLogic sessionLogic) {
		this.errorHandler = errorHandler;
		this.sessionLogic = sessionLogic;
	}

	@Override
	public ResponseSingle<Session> create(final Session session) {
		if (isBlank(session.getUsername())) {
			errorHandler.missingUsername();
		}
		if (isBlank(session.getPassword())) {
			errorHandler.missingPassword();
		}

		final String sessionId = sessionLogic.create(LoginDTO.newInstance() //
				.withLoginString(session.getUsername()) //
				.withPassword(session.getPassword()) //
				.withGroupName(session.getRole()) //
				.withServiceUsersAllowed(true) //
				.build());
		final OperationUser user = sessionLogic.getUser(sessionId);
		final CMGroup group = user.getPreferredGroup();

		return newResponseSingle(Session.class) //
				.withElement(newSession() //
						.withId(sessionId) //
						.withUsername(user.getAuthenticatedUser().getUsername()) //
						.withRole(group.isActive() ? group.getName() : null) //
						.withAvailableRoles(user.getAuthenticatedUser().getGroupNames()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Session> read(final String id) {
		if (!sessionLogic.exists(id)) {
			errorHandler.sessionNotFound(id);
		}

		final OperationUser user = sessionLogic.getUser(id);
		final CMGroup group = user.getPreferredGroup();

		return newResponseSingle(Session.class) //
				.withElement(newSession() //
						.withId(id) //
						.withUsername(user.getAuthenticatedUser().getUsername()) //
						.withRole(group.isActive() ? group.getName() : null) //
						.withAvailableRoles(user.getAuthenticatedUser().getGroupNames()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Session> update(final String id, final Session session) {
		if (!sessionLogic.exists(id)) {
			errorHandler.sessionNotFound(id);
		}

		if (isBlank(session.getRole())) {
			errorHandler.missingParam(GROUP);
		}

		final OperationUser user = sessionLogic.getUser(id);
		sessionLogic.update(id,
				LoginDTO.newInstance() //
						.withLoginString(user.getAuthenticatedUser().getUsername()) //
						.withGroupName(session.getRole()) //
						.withServiceUsersAllowed(true) //
						.build());

		final OperationUser updatedUser = sessionLogic.getUser(id);
		final CMGroup group = updatedUser.getPreferredGroup();

		return newResponseSingle(Session.class) //
				.withElement(newSession() //
						.withId(id) //
						.withUsername(updatedUser.getAuthenticatedUser().getUsername()) //
						.withRole(group.isActive() ? group.getName() : null) //
						.withAvailableRoles(updatedUser.getAuthenticatedUser().getGroupNames()) //
						.build()) //
				.build();
	}

	@Override
	public void delete(final String id) {
		if (!sessionLogic.exists(id)) {
			errorHandler.sessionNotFound(id);
		}

		sessionLogic.delete(id);
	}

}
