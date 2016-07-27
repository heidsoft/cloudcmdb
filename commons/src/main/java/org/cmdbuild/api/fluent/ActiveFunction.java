package org.cmdbuild.api.fluent;

abstract class ActiveFunction extends Function {

	private final FluentApiExecutor executor;

	ActiveFunction(final FluentApiExecutor executor, final String functionName) {
		super(functionName);
		this.executor = executor;
	}

	protected FluentApiExecutor executor() {
		return executor;
	}

}
