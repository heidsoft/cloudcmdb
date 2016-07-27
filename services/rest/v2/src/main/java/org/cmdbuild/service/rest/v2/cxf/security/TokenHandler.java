package org.cmdbuild.service.rest.v2.cxf.security;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.service.rest.v2.Unauthorized;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.StringFromMessage;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;

import com.google.common.base.Optional;

public class TokenHandler implements ContainerRequestFilter, LoggingSupport {

	private final StringFromMessage tokenFromMessage;
	private final SessionLogic sessionLogic;

	@Context
	private ResourceInfo resourceInfo;

	public TokenHandler(final StringFromMessage tokenFromMessage, final SessionLogic sessionLogic) {
		this.tokenFromMessage = tokenFromMessage;
		this.sessionLogic = sessionLogic;
	}

	/**
	 * Usable for tests only.
	 */
	public TokenHandler(final StringFromMessage tokenFromMessage, final SessionLogic sessionLogic,
			final ResourceInfo resourceInfo) {
		this(tokenFromMessage, sessionLogic);
		this.resourceInfo = resourceInfo;
	}

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		Response response = null;
		do {
			final boolean unauthorized = (findAnnotation(resourceInfo.getResourceMethod(), Unauthorized.class) != null);
			if (unauthorized) {
				break;
			}

			final Optional<String> token = tokenFromMessage.apply(requestContext);
			final boolean missingToken = !token.isPresent();
			if (missingToken) {
				response = Response.status(UNAUTHORIZED).build();
				break;
			}

			if (!sessionLogic.exists(token.get())) {
				response = Response.status(UNAUTHORIZED).build();
				break;
			}
			
			sessionLogic.setCurrent(token.get());
		} while (false);
		if (response != null) {
			requestContext.abortWith(response);
		}
	}

}
