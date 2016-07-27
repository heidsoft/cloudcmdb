package org.cmdbuild.listeners;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.cmdbuild.config.CmdbuildConfiguration;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public class HttpSessionBasedValuesStore implements ValuesStore {

	private final Supplier<Optional<HttpServletRequest>> supplier;
	private final CmdbuildConfiguration configuration;

	public HttpSessionBasedValuesStore(final Supplier<Optional<HttpServletRequest>> supplier,
			final CmdbuildConfiguration configuration) {
		this.supplier = supplier;
		this.configuration = configuration;
	}

	@Override
	public Object get(final String name) {
		final Optional<HttpSession> element = session();
		return element.isPresent() ? element.get().getAttribute(name) : null;
	}

	@Override
	public void set(final String name, final Object value) {
		final Optional<HttpSession> element = session();
		if (element.isPresent()) {
			element.get().setAttribute(name, value);
		}
	}

	@Override
	public void remove(final String name) {
		final Optional<HttpSession> element = session();
		if (element.isPresent()) {
			element.get().removeAttribute(name);
		}
	}

	private Optional<HttpSession> session() {
		final Optional<HttpSession> output;
		final Optional<HttpServletRequest> optional = supplier.get();
		if (optional.isPresent()) {
			final HttpServletRequest element = optional.get();
			HttpSession session = element.getSession(false);
			if (session == null) {
				session = element.getSession(true);
				final int sessionTimeout = configuration.getSessionTimeoutOrDefault();
				if (sessionTimeout > 0) {
					session.setMaxInactiveInterval(sessionTimeout);
				}
			}
			output = of(session);
		} else {
			output = absent();
		}
		return output;
	}

}
