package unit.cxf.security;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.cxf.security.FirstPresent.firstPresent;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import javax.ws.rs.container.ContainerRequestContext;

import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler.TokenExtractor;
import org.junit.Test;

import com.google.common.base.Optional;

public class FirstPresentTest {

	private static class DummyTokenExtractor implements TokenExtractor {

		private final Optional<String> optional;

		public DummyTokenExtractor(final Optional<String> optional) {
			this.optional = optional;
		}

		@Override
		public Optional<String> extract(final ContainerRequestContext value) {
			return optional;
		}

	}

	@Test
	public void firstReturned() throws Exception {
		// given
		final DummyTokenExtractor first = new DummyTokenExtractor(Optional.of("foo"));
		final DummyTokenExtractor second = new DummyTokenExtractor(Optional.of("bar"));
		final ContainerRequestContext value = mock(ContainerRequestContext.class);

		// when
		final Optional<String> optional = firstPresent(asList(first, second)).extract(value);

		// then
		assertThat(optional, equalTo(Optional.of("foo")));
	}

	@Test
	public void secondReturned() throws Exception {
		// given
		final DummyTokenExtractor first = new DummyTokenExtractor(Optional.<String> absent());
		final DummyTokenExtractor second = new DummyTokenExtractor(Optional.of("bar"));
		final ContainerRequestContext value = mock(ContainerRequestContext.class);

		// when
		final Optional<String> optional = firstPresent(asList(first, second)).extract(value);

		// then
		assertThat(optional, equalTo(Optional.of("bar")));
	}

}
