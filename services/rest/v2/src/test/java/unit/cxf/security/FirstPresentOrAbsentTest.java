package unit.cxf.security;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import javax.ws.rs.container.ContainerRequestContext;

import org.cmdbuild.service.rest.v2.cxf.util.Messages.FirstPresentOrAbsent;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.StringFromMessage;
import org.junit.Test;

import com.google.common.base.Optional;

public class FirstPresentOrAbsentTest {

	private static class Dummy implements StringFromMessage {

		private final Optional<String> optional;

		public Dummy(final Optional<String> optional) {
			this.optional = optional;
		}

		@Override
		public Optional<String> apply(final ContainerRequestContext input) {
			return optional;
		}

	}

	@Test
	public void firstReturned() throws Exception {
		// given
		final Dummy first = new Dummy(Optional.of("foo"));
		final Dummy second = new Dummy(Optional.of("bar"));
		final ContainerRequestContext value = mock(ContainerRequestContext.class);

		// when
		final Optional<String> optional = FirstPresentOrAbsent.of(asList(first, second)).apply(value);

		// then
		assertThat(optional, equalTo(Optional.of("foo")));
	}

	@Test
	public void secondReturned() throws Exception {
		// given
		final Dummy first = new Dummy(Optional.<String> absent());
		final Dummy second = new Dummy(Optional.of("bar"));
		final ContainerRequestContext value = mock(ContainerRequestContext.class);

		// when
		final Optional<String> optional = FirstPresentOrAbsent.of(asList(first, second)).apply(value);

		// then
		assertThat(optional, equalTo(Optional.of("bar")));
	}

}
