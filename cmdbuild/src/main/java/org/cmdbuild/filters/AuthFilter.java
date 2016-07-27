package org.cmdbuild.filters;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic.ClientAuthenticationResponse;
import org.cmdbuild.logic.auth.SessionLogic.Callback;
import org.cmdbuild.logic.auth.StandardSessionLogic;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration("AuthFilter")
@Scope(PROTOTYPE)
public class AuthFilter implements Filter {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(AuthFilter.class.getName());

	private static class ClientRequestWrapper implements ClientRequest {

		private final HttpServletRequest delegate;

		public ClientRequestWrapper(final HttpServletRequest delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getRequestUrl() {
			return delegate.getRequestURL().toString();
		}

		@Override
		public String getHeader(final String name) {
			return delegate.getHeader(name);
		}

		@Override
		public String getParameter(final String name) {
			return delegate.getParameter(name);
		}

	}

	public static final String LOGIN_URL = "index.jsp";
	public static final String LOGOUT_URL = "logout.jsp";

	private static boolean isRootPage(final String uri) {
		return uri.equals("/");
	}

	private static boolean isLoginPage(final String uri) {
		return uri.equals("/" + LOGIN_URL);
	}

	private static boolean isLogoutPage(final String uri) {
		return uri.equals("/" + LOGOUT_URL);
	}

	private static boolean isService(final String uri) {
		return uri.startsWith("/services/");
	}

	private static boolean isShark(final String uri) {
		return uri.startsWith("/shark/");
	}

	private static boolean isResouce(final String uri) {
		return uri.matches("^(.*)(css|js|png|jpg|gif|ico)$");
	}

	private static final String CMDBUILD_AUTHORIZATION = "CMDBuild-Authorization";
	private static final Cookie[] NO_COOKIES = new Cookie[] {};

	private static String sessionId(final HttpServletRequest httpRequest) {
		final Optional<String> header = ofNullable(defaultIfBlank(httpRequest.getHeader(CMDBUILD_AUTHORIZATION), null));
		final Optional<String> parameter = ofNullable(
				defaultIfBlank(httpRequest.getParameter(CMDBUILD_AUTHORIZATION), null));
		final Optional<String> cookie = stream(defaultIfNull(httpRequest.getCookies(), NO_COOKIES)) //
				.filter(input -> input.getName().equals(CMDBUILD_AUTHORIZATION)) //
				.findFirst() //
				.map(input -> input.getValue());
		final String output;
		if (header.isPresent()) {
			output = header.get();
		} else if (parameter.isPresent()) {
			output = parameter.get();
		} else if (cookie.isPresent()) {
			output = cookie.get();
		} else {
			output = null;
		}
		return output;
	}

	@Autowired
	private StandardSessionLogic sessionLogic;

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		// TODO do it in another way
		final AtomicReference<String> sessionId = new AtomicReference<>(sessionId(httpRequest));
		sessionLogic.setCurrent(sessionId.get());
		try {
			final String uri = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			logger.debug(marker, "request received for '{}'", uri);

			if (isRootPage(uri)) {
				logger.debug(marker, "root page, redirecting to login");
				redirectToLogin(httpResponse);
			} else if (isLoginPage(uri)) {
				if (sessionLogic.isValidUser(sessionId.get())) {
					redirectToManagement(httpResponse);
				} else {
					logger.debug(marker, "user is not valid, trying login using HTTP request");
					final ClientAuthenticationResponse clientAuthenticatorResponse = sessionLogic
							.create(new ClientRequestWrapper(httpRequest), new Callback() {

								@Override
								public void sessionCreated(final String id) {
									sessionId.set(id);
									sessionLogic.setCurrent(id);
								}

							});
					final String authenticationRedirectUrl = clientAuthenticatorResponse.getRedirectUrl();
					if (authenticationRedirectUrl != null) {
						redirectToCustom(authenticationRedirectUrl);
					} else if (sessionLogic.isValidUser(sessionId.get())) {
						redirectToManagement(httpResponse);
					}
				}
			} else if (!isService(uri) && !isShark(uri) && !isResouce(uri) && !isLoginPage(uri)) {
				if (!sessionLogic.isValidUser(sessionId.get())) {
					logger.debug(marker, "user is not valid, trying login using HTTP request");
					final ClientAuthenticationResponse clientAuthenticatorResponse = sessionLogic
							.create(new ClientRequestWrapper(httpRequest), new Callback() {

								@Override
								public void sessionCreated(final String id) {
									sessionId.set(id);
									sessionLogic.setCurrent(id);
								}

							});
					final String authenticationRedirectUrl = clientAuthenticatorResponse.getRedirectUrl();
					if (authenticationRedirectUrl != null) {
						redirectToCustom(authenticationRedirectUrl);
					} else if (!sessionLogic.isValidUser(sessionId.get()) && !isLogoutPage(uri)) {
						redirectToLogin(httpResponse);
					}
				}
			}
			filterChain.doFilter(request, response);
		} catch (final RedirectException re) {
			re.sendRedirect(httpResponse);
		}
	}

	private void redirectToManagement(final HttpServletResponse response) throws IOException, RedirectException {
		logger.debug(marker, "redirecting to management");
		throw new RedirectException("management.jsp");
	}

	private void redirectToLogin(final HttpServletResponse response) throws IOException, RedirectException {
		logger.debug(marker, "redirecting to login");
		throw new RedirectException(LOGIN_URL);
	}

	private void redirectToCustom(final String uri) throws IOException, RedirectException {
		logger.debug(marker, "redirecting to uri '{}'", uri);
		throw new RedirectException(uri);
	}

}
