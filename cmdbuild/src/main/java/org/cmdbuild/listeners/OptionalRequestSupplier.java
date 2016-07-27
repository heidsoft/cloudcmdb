package org.cmdbuild.listeners;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public class OptionalRequestSupplier implements Supplier<Optional<HttpServletRequest>> {

	private static final Optional<HttpServletRequest> ABSENT = Optional.absent();

	private final ContextStore store;

	public OptionalRequestSupplier(final ContextStore store) {
		this.store = store;
	}

	@Override
	public Optional<HttpServletRequest> get() {
		final Optional<CMDBContext> element = store.get();
		return element.isPresent() ? Optional.of(element.get().getRequest()) : ABSENT;
	}

}
