package integration.services.store.filter;

import static com.google.common.collect.Iterables.size;
import static java.lang.Integer.MAX_VALUE;
import static org.cmdbuild.common.Constants.ROLE_CLASS_NAME;
import static org.cmdbuild.services.store.filter.DataViewFilterStore.CLASS_NAME;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.services.store.filter.DataViewFilterStore;
import org.cmdbuild.services.store.filter.FilterConverter;
import org.cmdbuild.services.store.filter.FilterDTO;
import org.cmdbuild.services.store.filter.FilterStore;
import org.cmdbuild.services.store.filter.FilterStore.Filter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.IntegrationTestBase;

import com.google.common.collect.Iterables;

public class DataViewFilterStoreTest extends IntegrationTestBase {

	private static final long USER_ID = 123L;

	private DataViewFilterStore filterStore;
	private CMClass roleClass;
	private CMClass userClass;
	private final Long EMPTY_ID = null;

	@Before
	public void createFilterStore() throws Exception {
		filterStore = new DataViewFilterStore(dbDataView(), new FilterConverter(dbDataView()));
		roleClass = dbDataView().findClass(ROLE_CLASS_NAME);
		userClass = dbDataView().findClass("User");
	}

	@After
	public void clearSystemTables() throws Exception {
		final CMClass clazz = dbDataView().findClass(CLASS_NAME);
		dbDataView().clear(clazz);
	}

	@Test
	public void noFiltersDefinedAsDefault() throws Exception {
		// given

		// when
		final Iterable<FilterStore.Filter> filters = filterStore.readNonSharedFilters(null, USER_ID, 0, MAX_VALUE);

		// then
		assertThat(size(filters), equalTo(0));
	}

	@Test
	public void shouldFetchOnlyUserFilters() throws Exception {
		// given
		final Long id = filterStore.create(userFilter("foo", ROLE_CLASS_NAME, "bar"));
		filterStore.create(groupFilter("group_filter", "value", ROLE_CLASS_NAME, EMPTY_ID));

		// when
		final Iterable<FilterStore.Filter> filters = filterStore.readNonSharedFilters(null, USER_ID, 0, MAX_VALUE);

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(userFilter(id, "foo", ROLE_CLASS_NAME, "bar")));
	}

	@Test
	public void filterModified() throws Exception {
		// given
		final Long createdId = filterStore.create(userFilter("foo", ROLE_CLASS_NAME, "bar"));

		// when
		Iterable<FilterStore.Filter> filters = filterStore.readNonSharedFilters(ROLE_CLASS_NAME, USER_ID, 0, MAX_VALUE);

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(userFilter(createdId, "foo", ROLE_CLASS_NAME, "bar")));

		// but
		filterStore.update(userFilter(createdId, "foo", ROLE_CLASS_NAME, "baz"));

		// when
		filters = filterStore.readNonSharedFilters(null, USER_ID, 0, MAX_VALUE);

		// then
		assertThat(size(filters), equalTo(1));
		assertThat(filters, contains(userFilter(createdId, "foo", ROLE_CLASS_NAME, "baz")));
	}

	@Test
	public void filterDataFullyRead() throws Exception {
		// given
		filterStore.create(filter("foo", "bar", "baz", ROLE_CLASS_NAME, EMPTY_ID, false));

		// when
		final Filter filter = filterStore.readNonSharedFilters(null, USER_ID, 0, MAX_VALUE).iterator().next();

		// then
		assertThat(filter.getName(), equalTo("foo"));
		assertThat(filter.getDescription(), equalTo("bar"));
		assertThat(filter.getConfiguration(), equalTo("baz"));
	}

	@Test
	public void userCanHaveMoreThanOneFilterWithSameNameButForDifferentEntryType() throws Exception {
		// given
		filterStore.create(userFilter("name", ROLE_CLASS_NAME, "value"));
		filterStore.create(userFilter("name", userClass.getIdentifier().getLocalName(), "value2"));

		// when
		final Iterable<Filter> userFilters = filterStore.readNonSharedFilters(null, USER_ID, 0, MAX_VALUE);

		// then
		assertThat(Iterables.size(userFilters), equalTo(2));
	}

	@Test(expected = Exception.class)
	public void userCanHaveOnlyOneFilterWithSameNameAndEntryType() throws Exception {
		// when
		filterStore.create(userFilter("name", ROLE_CLASS_NAME, "value"));
		filterStore.create(userFilter("name", ROLE_CLASS_NAME, "value2"));
	}

	@Test
	public void testPagination() throws Exception {
		// given
		filterStore.create(userFilter("foo1", ROLE_CLASS_NAME, "value1"));
		filterStore.create(userFilter("foo2", ROLE_CLASS_NAME, "value2"));
		filterStore.create(userFilter("foo3", ROLE_CLASS_NAME, "value3"));
		filterStore.create(userFilter("foo4", ROLE_CLASS_NAME, "value4"));

		// when
		final PagedElements<Filter> userFilters = filterStore.readNonSharedFilters(roleClass.getIdentifier()
				.getLocalName(), USER_ID, 0, 2);

		// then
		assertEquals(4, userFilters.totalSize());
		assertEquals(2, Iterables.size(userFilters));
	}

	/*
	 * Utilities
	 */

	private Filter userFilter(final String name, final String className, final String value) {
		return userFilter(EMPTY_ID, name, className, value);
	}

	private Filter userFilter(final Long id, final String name, final String className, final String value) {
		return filter(name, name, value, className, id, false);
	}

	private Filter groupFilter(final String name, final String value, final String className, final Long id) {
		return filter(name, name, value, className, id, true);
	}

	private Filter filter(final String name, final String description, final String value, final String className,
			final Long id, final boolean asTemplate) {
		return FilterDTO.newFilter() //
				.withId(id) //
				.withName(name) //
				.withDescription(description) //
				.withClassName(className) //
				.withConfiguration(value) //
				.thatIsShared(asTemplate) //
				.withUserId(USER_ID) //
				.build();
	}

}
