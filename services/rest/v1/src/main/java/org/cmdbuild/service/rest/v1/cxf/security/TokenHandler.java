package org.cmdbuild.service.rest.v1.cxf.security;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.service.rest.v1.Unauthorized;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;

import com.google.common.base.Optional;

public class TokenHandler implements ContainerRequestFilter, LoggingSupport {

	public static interface TokenExtractor {

		Optional<String> extract(ContainerRequestContext value);

	}

	private final TokenExtractor tokenExtractor;
	private final SessionLogic sessionLogic;

	@Context
	private ResourceInfo resourceInfo;

	public TokenHandler(final TokenExtractor tokenExtractor, final SessionLogic sessionLogic) {
		this.tokenExtractor = tokenExtractor;
		this.sessionLogic = sessionLogic;
	}

	/**
	 * Usable for tests only.
	 */
	public TokenHandler(final TokenExtractor tokenExtractor, final SessionLogic sessionLogic,
			final ResourceInfo resourceInfo) {
		this(tokenExtractor, sessionLogic);
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

			final Optional<String> token = tokenExtractor.extract(requestContext);
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
