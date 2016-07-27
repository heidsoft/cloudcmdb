package org.cmdbuild.service.rest.v2.cxf.localization;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.cmdbuild.service.rest.v2.cxf.util.Messages.StringFromMessage;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;

import com.google.common.base.Optional;

public class LocalizationHandler implements ContainerRequestFilter, LoggingSupport {

	private final StringFromMessage localizedFromMessage;
	private final StringFromMessage localizationFromMessage;
	private final org.cmdbuild.services.localization.RequestHandler requestHandler;

	public LocalizationHandler(final StringFromMessage localized, final StringFromMessage localization,
			final org.cmdbuild.services.localization.RequestHandler requestHandler) {
		this.localizedFromMessage = localized;
		this.localizationFromMessage = localization;
		this.requestHandler = requestHandler;
	}

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		final Optional<String> localized = localizedFromMessage.apply(requestContext);
		final Optional<String> localization = localizationFromMessage.apply(requestContext);
		requestHandler.setLocalized(localized.isPresent() ? toBoolean(localized.get()) : false);
		requestHandler.setLocalization(localization.isPresent() ? localization.get() : null);
	}

}
