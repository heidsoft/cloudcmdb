package unit.common.utils.guava;

import static java.util.Arrays.asList;
import static org.cmdbuild.common.utils.guava.Suppliers.firstNotNull;
import static org.cmdbuild.common.utils.guava.Suppliers.nullOnException;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.base.Supplier;

public class SuppliersTest {

	@Test
	public void nullIsReturnedInsteadOfThrowingException() throws Exception {
		// given
		final Supplier<Object> throwsException = new Supplier<Object>() {

			@Override
			public Object get() {
				throw new RuntimeException();
			}

		};

		// when
		final Object value = nullOnException(throwsException).get();

		// then
		assertThat(value, is(nullValue()));
	}

	@Test
	public void firstNotNullValueIsReturned() throws Exception {
		// given
		final Supplier<Object> returnsNull = new Supplier<Object>() {

			@Override
			public Object get() {
				return null;
			}

		};
		final Supplier<Object> returnsFoo = new Supplier<Object>() {

			@Override
			public Object get() {
				return "foo";
			}

		};
		final Supplier<Object> returnsBar = new Supplier<Object>() {

			@Override
			public Object get() {
				return "bar";
			}

		};

		// when
		final Object value = firstNotNull(asList(returnsNull, returnsFoo, returnsBar)).get();

		// then
		assertThat(value, equalTo(Object.class.cast("foo")));
	}

}
