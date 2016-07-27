package org.cmdbuild.model.widget.customform;

class InvalidDataBuilder implements DataBuilder {

	private final String message;

	public InvalidDataBuilder(final String message) {
		this.message = message;
	}

	@Override
	public String build() {
		throw new RuntimeException(message);
	}

}