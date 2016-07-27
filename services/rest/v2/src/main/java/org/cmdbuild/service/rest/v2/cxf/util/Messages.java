package org.cmdbuild.service.rest.v2.cxf.util;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.v2.logging.LoggingSupport;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class Messages {

	public static interface StringFromMessage extends Function<ContainerRequestContext, Optional<String>> {

		Optional<String> ABSENT = Optional.absent();

		@Override
		Optional<String> apply(ContainerRequestContext input);

	}

	public static class FirstPresentOrAbsent implements StringFromMessage {

		public static FirstPresentOrAbsent of(final Iterable<? extends StringFromMessage> elements) {
			return new FirstPresentOrAbsent(elements);
		}

		private final Iterable<? extends StringFromMessage> elements;

		private FirstPresentOrAbsent(final Iterable<? extends StringFromMessage> elements) {
			this.elements = elements;
		}

		@Override
		public Optional<String> apply(final ContainerRequestContext input) {
			for (final StringFromMessage element : elements) {
				final Optional<String> optional = element.apply(input);
				if (optional.isPresent()) {
					return optional;
				}
			}
			return ABSENT;
		}

	}

	public static class HeaderValue implements StringFromMessage {

		public static HeaderValue of(final String name) {
			return new HeaderValue(name);
		}

		private static final Map<String, List<String>> NO_HEADERS = emptyMap();

		private final String name;

		private HeaderValue(final String name) {
			this.name = name;
		}

		@Override
		public Optional<String> apply(final ContainerRequestContext input) {
			final MultivaluedMap<String, String> headers = input.getHeaders();
			final List<String> tokens = defaultIfNull(headers, NO_HEADERS).get(name);
			return (tokens == null || tokens.isEmpty()) ? ABSENT : Optional.of(tokens.get(0));
		}

	}

	public static class ParameterValue implements StringFromMessage, LoggingSupport {

		public static ParameterValue of(final String name) {
			return new ParameterValue(name);
		}

		private static final Map<String, List<String>> NO_PARAMETERS = emptyMap();

		private final String name;

		private ParameterValue(final String name) {
			this.name = name;
		}

		@Override
		public Optional<String> apply(final ContainerRequestContext input) {
			final MultivaluedMap<String, String> parameters = input.getUriInfo().getQueryParameters(true);
			final List<String> tokens = defaultIfNull(parameters, NO_PARAMETERS).get(name);
			return (tokens == null || tokens.isEmpty()) ? ABSENT : Optional.of(tokens.get(0));
		}

	}

	private Messages() {
		// prevents instantiation
	}

}
