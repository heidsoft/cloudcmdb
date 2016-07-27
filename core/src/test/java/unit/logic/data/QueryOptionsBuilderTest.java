package unit.logic.data;

import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.data.QueryOptions;
import org.json.JSONArray;
import org.junit.Test;

public class QueryOptionsBuilderTest {

	private static final String EMPTY_OBJECT = "{}";
	private static final String EMPTY_ARRAY = "[]";

	@Test
	public void shouldCreateQueryOptionsWithDefaultValues() {
		// when
		final QueryOptions options = QueryOptions.newQueryOption()//
				.build();

		// then
		assertThat(options.getLimit(), equalTo(Integer.MAX_VALUE));
		assertThat(options.getOffset(), equalTo(0));
		assertThat(options.getFilter().toString(), equalTo(EMPTY_OBJECT));
		assertThat(options.getSorters().toString(), equalTo(EMPTY_ARRAY));
		assertThat(options.getAttributes().toString(), equalTo(EMPTY_ARRAY));
	}

	@Test
	public void shouldReturnLimitAndOffsetValuesWhenSet() {
		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.limit(10) //
				.offset(3) //
				.build();

		// then
		assertThat(options.getLimit(), equalTo(10));
		assertThat(options.getOffset(), equalTo(3));
		assertThat(options.getFilter().toString(), equalTo(EMPTY_OBJECT));
		assertThat(options.getSorters().toString(), equalTo(EMPTY_ARRAY));
		assertThat(options.getAttributes().toString(), equalTo(EMPTY_ARRAY));
	}

	@Test
	public void shouldReturnSortersWhenSet() throws Exception {
		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.orderBy(new JSONArray("[a, b, 'c,']")) //
				.build();

		// then
		assertThat(options.getSorters().length(), equalTo(3));
	}

	@Test
	public void shouldReturnEmptyArrayIfSortersIsNull() throws Exception {
		// given
		final JSONArray sorters = null;

		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.orderBy(sorters) //
				.build();

		// then
		assertThat(options.getSorters().toString(), equalTo(EMPTY_ARRAY));
	}

	@Test
	public void shouldReturnAttributesWhenSet() throws Exception {
		// given
		final Iterable<String> attributes = asList("foo", "bar", "baz");

		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.onlyAttributes(attributes) //
				.build();

		// then
		assertThat(size(options.getAttributes()), equalTo(3));
	}

	@Test
	public void shouldReturnEmptyArrayIfAttributesIsNull() throws Exception {
		// given
		final Iterable<String> attributes = null;

		// when
		final QueryOptions options = QueryOptions.newQueryOption() //
				.onlyAttributes(attributes) //
				.build();

		// then
		assertThat(size(options.getAttributes()), equalTo(0));
	}

}
