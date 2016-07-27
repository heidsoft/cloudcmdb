package unit.cxf.security;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.cxf.security.Token.TOKEN_KEY;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.cmdbuild.service.rest.v1.cxf.security.HeaderTokenExtractor;
import org.junit.Test;

import com.google.common.base.Optional;

public class HeaderTokenExtractorTest {

	@Test
	public void absentWhenNullHeaders() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		doReturn(null) //
				.when(containerRequestContext).getHeaders();

		// when
		final Optional<String> optional = new HeaderTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getHeaders();
	}

	@Test
	public void absentWhenEmptyHeaders() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		doReturn(new MultivaluedHashMap<String, String>()) //
				.when(containerRequestContext).getHeaders();

		// when
		final Optional<String> optional = new HeaderTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getHeaders();
	}

	@Test
	public void absentWhenGettingTokenKeyReturnsEmptyList() throws Exception {
		// given
		final MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();
		headers.put(TOKEN_KEY, new ArrayList<String>());
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		doReturn(headers) //
				.when(containerRequestContext).getHeaders();

		// when
		final Optional<String> optional = new HeaderTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getHeaders();
	}

	@Test
	public void valuedWithTheFirstElementOfTheList() throws Exception {
		// given
		final MultivaluedMap<String, String> headers = new MultivaluedHashMap<String, String>();
		headers.put(TOKEN_KEY, new ArrayList<String>(asList("foo", "bar")));
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		doReturn(headers) //
				.when(containerRequestContext).getHeaders();

		// when
		final Optional<String> optional = new HeaderTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.of("foo")));
		verify(containerRequestContext).getHeaders();
	}

}
