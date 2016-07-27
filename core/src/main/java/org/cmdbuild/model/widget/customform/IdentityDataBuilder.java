package org.cmdbuild.model.widget.customform;

class IdentityDataBuilder implements DataBuilder {

	private final String value;

	public IdentityDataBuilder(final String value) {
		this.value = value;
	}

	@Override
	public String build() {
		return value;
	}

}