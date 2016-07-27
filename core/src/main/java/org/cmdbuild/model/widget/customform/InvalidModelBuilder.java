package org.cmdbuild.model.widget.customform;

class InvalidModelBuilder implements ModelBuilder {

	private final String message;

	public InvalidModelBuilder(final String message) {
		this.message = message;
	}

	@Override
	public String build() {
		throw new RuntimeException(message);
	}

}