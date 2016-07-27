package org.cmdbuild.api.fluent;

public class NewCard extends ActiveCard {

	NewCard(final FluentApiExecutor executor, final String className) {
		super(executor, className, null);
	}

	public NewCard withCode(final String value) {
		super.setCode(value);
		return this;
	}

	public NewCard withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public NewCard with(final String name, final Object value) {
		return withAttribute(name, value);
	}

	public NewCard withAttribute(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public CardDescriptor create() {
		return executor().create(this);
	}

}