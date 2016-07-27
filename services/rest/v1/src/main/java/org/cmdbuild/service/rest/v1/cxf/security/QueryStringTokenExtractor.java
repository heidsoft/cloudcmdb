package org.cmdbuild.service.rest.v1.cxf.security;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.v1.cxf.security.Token.TOKEN_KEY;

import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;

import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler.TokenExtractor;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;

import com.google.common.base.Optional;

public class QueryStringTokenExtractor implements TokenExtractor, LoggingSupport {

	private static final Map<String, List<String>> NO_PARAMETERS = emptyMap();
	private static final Optional<String> ABSENT = Optional.absent();

	@Override
	public Optional<String> extract(final ContainerRequestContext value) {
		final Map<String, List<String>> values = value.getUriInfo().getQueryParameters(true);
		final List<String> tokens = defaultIfNull(values, NO_PARAMETERS).get(TOKEN_KEY);
		return (tokens == null || tokens.isEmpty()) ? ABSENT : Optional.of(tokens.get(0));
	}

}
