package unit.cxf.security;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cmdbuild.logic.auth.SessionLogic;
import org.cmdbuild.service.rest.v1.Unauthorized;
import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler.TokenExtractor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class TokenHandlerTest {

	private static interface Dummy {

		void dummy();

	}

	private static class DummyUnauthorized implements Dummy {

		@Override
		@Unauthorized
		public void dummy() {
		}

	}

	private static class DummyAuthorized implements Dummy {

		@Override
		public void dummy() {
		}

	}

	private TokenExtractor tokenExtractor;
	private SessionLogic sessionLogic;

	@Before
	public void setUp() throws Exception {
		tokenExtractor = mock(TokenExtractor.class);
		sessionLogic = mock(SessionLogic.class);
	}

	@Test
	public void unauthorizedServicesHaveNoOtherRequirements() throws Exception {
		// given
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyUnauthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionLogic, resourceInfo);
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verifyNoMoreInteractions(tokenExtractor, sessionLogic, resourceInfo, requestContext);
	}

	@Test
	public void unauthorizedResponseForAuthorizedServiceWhenNoTokenReceiced() throws Exception {
		// given
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyAuthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionLogic, resourceInfo);
		doReturn(absent()) //
				.when(tokenExtractor).extract(any(ContainerRequestContext.class));
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verify(tokenExtractor).extract(requestContext);
		final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(captor.capture());
		verifyNoMoreInteractions(tokenExtractor, sessionLogic, resourceInfo, requestContext);

		assertThat(captor.getValue().getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void unauthorizedResponseResponseForAuthorizedServiceWhenInvalidTokenReceiced() throws Exception {
		// given
		doReturn(false) //
				.when(sessionLogic).exists(anyString());
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyAuthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionLogic, resourceInfo);
		doReturn(of("foo")) //
				.when(tokenExtractor).extract(any(ContainerRequestContext.class));
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verify(tokenExtractor).extract(requestContext);
		verify(sessionLogic).exists(eq("foo"));
		final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(captor.capture());
		verifyNoMoreInteractions(tokenExtractor, sessionLogic, resourceInfo, requestContext);

		assertThat(captor.getValue().getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void unauthorizedResponseForAuthorizedServiceWhenExistingTokenReceicedButMissingOperationUser()
			throws Exception {
		// given
		doReturn(false) //
				.when(sessionLogic).exists(anyString());
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyAuthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionLogic, resourceInfo);
		doReturn(of("foo")) //
				.when(tokenExtractor).extract(any(ContainerRequestContext.class));
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verify(tokenExtractor).extract(requestContext);
		verify(sessionLogic).exists(eq("foo"));
		final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(captor.capture());
		verifyNoMoreInteractions(tokenExtractor, sessionLogic, resourceInfo, requestContext);

		assertThat(captor.getValue().getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void nullResponseForAuthorizedServiceWhenExistingTokenReceicedAndExistingOperationUser() throws Exception {
		// given
		doReturn(true) //
				.when(sessionLogic).exists(anyString());
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyAuthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionLogic, resourceInfo);
		doReturn(of("foo")) //
				.when(tokenExtractor).extract(any(ContainerRequestContext.class));
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verify(tokenExtractor).extract(requestContext);
		verify(sessionLogic).exists(eq("foo"));
		verify(sessionLogic).setCurrent(eq("foo"));
		verifyNoMoreInteractions(tokenExtractor, sessionLogic, resourceInfo, requestContext);
	}

}
