package org.cmdbuild.listeners;

import com.google.common.base.Optional;

public class ThreadLocalContextStore implements ContextStore {

	private static final Optional<CMDBContext> ABSENT = Optional.absent();

	private static final ThreadLocal<CMDBContext> requestContext = new ThreadLocal<CMDBContext>();

	@Override
	public Optional<CMDBContext> get() {
		final CMDBContext reference = requestContext.get();
		return (reference == null) ? ABSENT : Optional.of(reference);
	}

	@Override
	public void set(final CMDBContext value) {
		requestContext.set(value);
	}

	@Override
	public void remove() {
		requestContext.remove();
	}

}
