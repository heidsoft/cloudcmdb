package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingNotifier extends ForwardingObject implements ErrorNotifier {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingNotifier() {
	}

	@Override
	protected abstract ErrorNotifier delegate();

	@Override
	public void unsupportedIdentifier(final String identifier) {
		delegate().unsupportedIdentifier(identifier);
	}

	@Override
	public void unsupportedType(final String type) {
		delegate().unsupportedType(type);
	}

	@Override
	public void unsupportedField(final String field) {
		delegate().unsupportedField(field);
	}

	@Override
	public void invalidConverter() {
		delegate().invalidConverter();
	}

}
