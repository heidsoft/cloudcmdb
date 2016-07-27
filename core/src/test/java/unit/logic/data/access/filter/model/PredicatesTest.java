package unit.logic.data.access.filter.model;

import static org.cmdbuild.logic.data.access.filter.model.Predicates.and;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.contains;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.endsWith;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.equalTo;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.greaterThan;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.in;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.isNull;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.lessThan;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.like;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.not;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.or;
import static org.cmdbuild.logic.data.access.filter.model.Predicates.startsWith;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.data.access.filter.model.Predicate;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

public class PredicatesTest {

	@Test
	public void andTest() throws Exception {
		// given
		final Predicate first = and(equalTo("foo"));
		final Predicate second = and(equalTo("foo"));
		final Predicate third = and(equalTo("bar"));

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void containsTest() throws Exception {
		// given
		final Predicate first = contains("foo");
		final Predicate second = contains("foo");
		final Predicate third = contains("bar");

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void endsWithTest() throws Exception {
		// given
		final Predicate first = endsWith("foo");
		final Predicate second = endsWith("foo");
		final Predicate third = endsWith("bar");

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void equalToTest() throws Exception {
		// given
		final Predicate first = equalTo("foo");
		final Predicate second = equalTo("foo");
		final Predicate third = equalTo("bar");

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void greaterThanTest() throws Exception {
		// given
		final Predicate first = greaterThan("foo");
		final Predicate second = greaterThan("foo");
		final Predicate third = greaterThan("bar");

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void inTest() throws Exception {
		// given
		final Predicate first = in("foo");
		final Predicate second = in("foo");
		final Predicate third = in("bar");

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void isNullTest() throws Exception {
		// given
		final Predicate first = isNull();
		final Predicate second = isNull();

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
	}

	@Test
	public void lessThanTest() throws Exception {
		// given
		final Predicate first = lessThan("foo");
		final Predicate second = lessThan("foo");
		final Predicate third = lessThan("bar");

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void likeTest() throws Exception {
		// given
		final Predicate first = like("foo");
		final Predicate second = like("foo");
		final Predicate third = like("bar");

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void notTest() throws Exception {
		// given
		final Predicate first = not(like("foo"));
		final Predicate second = not(like("foo"));
		final Predicate third = not(like("bar"));

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void orTest() throws Exception {
		// given
		final Predicate first = or(equalTo("foo"));
		final Predicate second = or(equalTo("foo"));
		final Predicate third = or(equalTo("bar"));

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	@Test
	public void startsWithTest() throws Exception {
		// given
		final Predicate first = startsWith("foo");
		final Predicate second = startsWith("foo");
		final Predicate third = startsWith("bar");

		// then
		assertThat(first, _equalTo(second));
		assertThat(first.hashCode(), _equalTo(second.hashCode()));
		assertThat(first, _not(_equalTo(third)));
		assertThat(first.hashCode(), _not(_equalTo(third.hashCode())));
	}

	private static <T> Matcher<T> _equalTo(final T operand) {
		return Matchers.equalTo(operand);
	}

	private static <T> Matcher<T> _not(final Matcher<T> matcher) {
		return Matchers.not(matcher);
	}

}
