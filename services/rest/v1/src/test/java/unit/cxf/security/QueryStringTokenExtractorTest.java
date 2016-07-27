package unit.cxf.security;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.cxf.security.Token.TOKEN_KEY;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.cmdbuild.service.rest.v1.cxf.security.QueryStringTokenExtractor;
import org.junit.Test;

import com.google.common.base.Optional;

public class QueryStringTokenExtractorTest {

	@Test
	public void absentWhenQueryStringIsNull() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		final UriInfo uriInfo = mock(UriInfo.class);
		doReturn(uriInfo) //
				.when(containerRequestContext).getUriInfo();
		doReturn(null) //
				.when(uriInfo).getQueryParameters(anyBoolean());

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getUriInfo();
		verify(uriInfo).getQueryParameters(true);
	}

	@Test
	public void absentWhenQueryStringIsEmpty() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		final UriInfo uriInfo = mock(UriInfo.class);
		doReturn(uriInfo) //
				.when(containerRequestContext).getUriInfo();
		doReturn(new MultivaluedHashMap<String, String>()) //
				.when(uriInfo).getQueryParameters(anyBoolean());

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getUriInfo();
		verify(uriInfo).getQueryParameters(true);
	}

	@Test
	public void absentWhenQueryStringIsDummyNameOnly() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		final UriInfo uriInfo = mock(UriInfo.class);
		doReturn(uriInfo) //
				.when(containerRequestContext).getUriInfo();
		doReturn(new MultivaluedHashMap<String, String>() {
			{
				putSingle("foo", null);
			}
		}) //
				.when(uriInfo).getQueryParameters(anyBoolean());

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getUriInfo();
		verify(uriInfo).getQueryParameters(true);
	}

	@Test
	public void absentWhenQueryStringIsDummyNamesOnly() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		final UriInfo uriInfo = mock(UriInfo.class);
		doReturn(uriInfo) //
				.when(containerRequestContext).getUriInfo();
		doReturn(new MultivaluedHashMap<String, String>() {
			{
				putSingle("foo", null);
				putSingle("bar", null);
			}
		}) //
				.when(uriInfo).getQueryParameters(anyBoolean());

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getUriInfo();
		verify(uriInfo).getQueryParameters(true);
	}

	@Test
	public void absentWhenQueryStringIsDummyNameAndValues() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		final UriInfo uriInfo = mock(UriInfo.class);
		doReturn(uriInfo) //
				.when(containerRequestContext).getUriInfo();
		doReturn(new MultivaluedHashMap<String, String>() {
			{
				putSingle("foo", "FOO");
				putSingle("bar", "BAR");
			}
		}) //
				.when(uriInfo).getQueryParameters(anyBoolean());

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getUriInfo();
		verify(uriInfo).getQueryParameters(true);
	}

	@Test
	public void absentWhenQueryStringIsTokenKeyOnly() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		final UriInfo uriInfo = mock(UriInfo.class);
		doReturn(uriInfo) //
				.when(containerRequestContext).getUriInfo();
		doReturn(new MultivaluedHashMap<String, String>() {
			{
				putSingle(TOKEN_KEY, null);
			}
		}) //
				.when(uriInfo).getQueryParameters(anyBoolean());

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.<String> absent()));
		verify(containerRequestContext).getUriInfo();
		verify(uriInfo).getQueryParameters(true);
	}

	@Test
	public void valuedWithTheFirstOccurrence() throws Exception {
		// given
		final ContainerRequestContext containerRequestContext = mock(ContainerRequestContext.class);
		final UriInfo uriInfo = mock(UriInfo.class);
		doReturn(uriInfo) //
				.when(containerRequestContext).getUriInfo();
		doReturn(new MultivaluedHashMap<String, String>() {
			{
				putSingle("foo", "FOO");
				put(TOKEN_KEY, asList("12345678", "abcdefgh"));
				putSingle("bar", "BAR");
				putSingle("baz", "BAZ");
			}
		}) //
				.when(uriInfo).getQueryParameters(anyBoolean());

		// when
		final Optional<String> optional = new QueryStringTokenExtractor().extract(containerRequestContext);

		// then
		assertThat(optional, equalTo(Optional.of("12345678")));
		verify(containerRequestContext).getUriInfo();
		verify(uriInfo).getQueryParameters(true);
	}

}
