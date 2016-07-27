package unit.core.api.fluent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.Lookup;
import org.cmdbuild.api.fluent.QueryAllLookup;
import org.cmdbuild.api.fluent.QuerySingleLookup;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.core.api.fluent.LogicFluentApiExecutor;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic.LookupQuery;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class FetchLookupTest {

	private static final String D1 = "d1";
	private static final String C1 = "c1";
	private FluentApiExecutor executor;
	private DataAccessLogic dataLogic;
	private LookupLogic lookupLogic;

	@Before
	public void setUp() throws Exception {
		dataLogic = mock(DataAccessLogic.class);
		lookupLogic = mock(LookupLogic.class);
		executor = new LogicFluentApiExecutor(dataLogic, lookupLogic);
	}

	@Test
	public void fetchAllLookupOfValue() throws Exception {
		// given
		final org.cmdbuild.data.store.lookup.Lookup first = //
		org.cmdbuild.data.store.lookup.LookupImpl.newInstance()//
				.withId((long) 1)//
				.withCode(C1) //
				.withDescription(D1) //
				.build();
		final org.cmdbuild.data.store.lookup.Lookup second = //
		org.cmdbuild.data.store.lookup.LookupImpl.newInstance()//
				.withId((long) 2)//
				.withCode("c2") //
				.withDescription("d2") //
				.build();
		final List<org.cmdbuild.data.store.lookup.Lookup> values = Arrays.asList(first, second);
		final PagedElements<org.cmdbuild.data.store.lookup.Lookup> pagedValues = new PagedElements<org.cmdbuild.data.store.lookup.Lookup>(
				values, values.size());
		when(lookupLogic.getAllLookup(any(LookupType.class), any(Boolean.class), any(LookupQuery.class))) //
				.thenReturn(pagedValues);
		final QueryAllLookup queryLookup = mock(QueryAllLookup.class);
		when(queryLookup.getType()) //
				.thenReturn("thetype");

		// when
		final Iterable<Lookup> elements = executor.fetch(queryLookup);

		// then
		verify(lookupLogic).getAllLookup(any(LookupType.class), any(Boolean.class), any(LookupQuery.class));
		verifyZeroInteractions(dataLogic);
		final Lookup _first = Iterables.get(elements, 0);
		final Lookup _second = Iterables.get(elements, 1);
		assertThat(C1, equalTo(_first.getCode()));
		assertThat(D1, equalTo(_first.getDescription()));
		assertThat("c2", equalTo(_second.getCode()));
		assertThat("d2", equalTo(_second.getDescription()));
	}

	@Test
	public void fetchSingleLookupById() throws Exception {
		// given
		final QuerySingleLookup querySingleLookup = mock(QuerySingleLookup.class);
		final org.cmdbuild.data.store.lookup.LookupImpl value = org.cmdbuild.data.store.lookup.LookupImpl.newInstance()//
				.withId((long) 1)//
				.withCode(C1) //
				.withDescription(D1) //
				.build();
		when(lookupLogic.getLookup(any(Long.class))) //
				.thenReturn(value);

		// when
		final Lookup result = executor.fetch(querySingleLookup);

		// then
		verify(lookupLogic).getLookup(any(Long.class));
		verifyZeroInteractions(dataLogic);
		assertThat(result.getCode(), equalTo(C1));
	}
}
