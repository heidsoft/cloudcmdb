package org.cmdbuild.api.fluent;

abstract class ActiveRelation extends Relation {

	private final FluentApiExecutor executor;

	ActiveRelation(final FluentApiExecutor executor, final String domainName) {
		super(domainName);
		this.executor = executor;
	}

	protected FluentApiExecutor executor() {
		return executor;
	}

}
