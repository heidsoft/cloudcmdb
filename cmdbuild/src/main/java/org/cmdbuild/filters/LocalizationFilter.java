package org.cmdbuild.filters;

import static java.util.Arrays.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.services.localization.RequestHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Optional;

/**
 * Sets the localization parameter for the current request.
 */
@Configuration("LocalizationFilter")
public class LocalizationFilter implements Filter, ApplicationContextAware {

	private static final String HEADER_PREFIX = "CMDBuild-";
	private static final String LOCALIZED_HEADER = HEADER_PREFIX + "Localized";
	private static final String LOCALIZATION_HEADER = HEADER_PREFIX + "Localization";

	private static interface Extractor {

		Optional<String> ABSENT = Optional.absent();

		Optional<String> extract(ServletRequest request);

	}

	private static class HeaderExtractor implements Extractor {

		public static HeaderExtractor of(final String name) {
			return new HeaderExtractor(name);
		}

		private final String name;

		private HeaderExtractor(final String name) {
			this.name = name;
		}

		@Override
		public Optional<String> extract(final ServletRequest request) {
			final String value;
			if (request instanceof HttpServletRequest) {
				final HttpServletRequest httpRequest = HttpServletRequest.class.cast(request);
				value = httpRequest.getHeader(name);
			} else {
				value = null;
			}
			return (value == null) ? ABSENT : Optional.of(value);
		}

	}

	private static class FirstPresentOrAbsentExtractor implements Extractor {

		public static FirstPresentOrAbsentExtractor of(final Extractor... elements) {
			return of(asList(elements));
		}

		public static FirstPresentOrAbsentExtractor of(final Iterable<Extractor> elements) {
			return new FirstPresentOrAbsentExtractor(elements);
		}

		private final Iterable<Extractor> elements;

		private FirstPresentOrAbsentExtractor(final Iterable<Extractor> elements) {
			this.elements = elements;
		}

		@Override
		public Optional<String> extract(final ServletRequest request) {
			for (final Extractor element : elements) {
				final Optional<String> value = element.extract(request);
				if (value.isPresent()) {
					return value;
				}
			}
			return ABSENT;
		}

	}

	private RequestHandler requestHandler;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.requestHandler = applicationContext.getBean(RequestHandler.class);
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {
		// TODO log
		final Optional<String> localized = FirstPresentOrAbsentExtractor.of( //
				HeaderExtractor.of(LOCALIZED_HEADER) //
				) //
				.extract(request);
		final Optional<String> localization = FirstPresentOrAbsentExtractor.of( //
				HeaderExtractor.of(LOCALIZATION_HEADER) //
				) //
				.extract(request);
		requestHandler.setLocalized(localized.isPresent() ? toBoolean(localized.get()) : false);
		requestHandler.setLocalization(localization.isPresent() ? localization.get() : null);
		filterChain.doFilter(request, response);
	}

}
