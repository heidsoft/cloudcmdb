package org.cmdbuild.api.fluent;

abstract class ActiveCard extends Card {

	private final FluentApiExecutor executor;

	ActiveCard(final FluentApiExecutor executor, final String className, final Integer id) {
		super(className, id);
		this.executor = executor;
	}

	protected FluentApiExecutor executor() {
		return executor;
	}

}
