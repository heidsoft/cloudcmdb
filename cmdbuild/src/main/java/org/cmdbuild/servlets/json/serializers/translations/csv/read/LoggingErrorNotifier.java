package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import org.cmdbuild.logger.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LoggingErrorNotifier extends ForwardingNotifier {

	private static final Logger logger = Log.JSONRPC;
	private static final Marker marker = MarkerFactory.getMarker(LoggingErrorNotifier.class.getName());

	private final ErrorNotifier delegate;

	private LoggingErrorNotifier(final ErrorNotifier delegate) {
		this.delegate = delegate;
	}

	public static ErrorNotifier of(final ErrorNotifier delegate) {
		return new LoggingErrorNotifier(delegate);
	}

	@Override
	protected ErrorNotifier delegate() {
		return delegate;
	}

	@Override
	public void unsupportedIdentifier(final String identifier) {
		final String message = String.format(MSG_UNSUPPORTED_IDENTIFIER, identifier);
		log(message);
		delegate().unsupportedIdentifier(identifier);
	}

	@Override
	public void unsupportedType(final String type) {
		final String message = String.format(MSG_UNSUPPORTED_TYPE, type);
		log(message);
		delegate().unsupportedType(type);
	}

	@Override
	public void unsupportedField(final String field) {
		final String message = String.format(MSG_UNSUPPORTED_FIELD, field);
		log(message);
		delegate().unsupportedField(field);
	}

	@Override
	public void invalidConverter() {
		final String message = MSG_INVALID_CONVERTER;
		log(message);
		delegate().invalidConverter();
	}

	private void log(final String message) {
		logger.warn(marker, message);
	}

}
