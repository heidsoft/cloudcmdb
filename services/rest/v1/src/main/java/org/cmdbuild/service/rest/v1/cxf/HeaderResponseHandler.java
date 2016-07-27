package org.cmdbuild.service.rest.v1.cxf;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.activation.DataHandler;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.cmdbuild.common.logging.LoggingSupport;

public class HeaderResponseHandler implements ContainerResponseFilter, LoggingSupport {

	@Override
	public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
			throws IOException {
		final Object entity = responseContext.getEntity();
		if (entity instanceof DataHandler) {
			final DataHandler dataHandler = DataHandler.class.cast(entity);
			responseContext.getHeaders() //
					.add("Content-Disposition", format("inline; filename=\"%s\"", _encode(dataHandler.getName())));
		}
	}

	private static String _encode(final String name) {
		try {
			return encode(name, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			logger.error("error encoding name", e);
			return name;
		}
	}
}
