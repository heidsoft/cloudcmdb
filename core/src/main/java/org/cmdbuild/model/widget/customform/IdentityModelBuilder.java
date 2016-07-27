package org.cmdbuild.model.widget.customform;

class IdentityModelBuilder implements ModelBuilder {

	private final String value;

	public IdentityModelBuilder(final String value) {
		this.value = value;
	}

	@Override
	public String build() {
		return value;
	}

}