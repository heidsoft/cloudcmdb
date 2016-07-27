package unit.dao.view.user.privileges;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.user.privileges.PartiallyCachingRowAndColumnPrivilegeFetcher;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;
import org.junit.Before;
import org.junit.Test;

public class PartiallyCachingRowAndColumnPrivilegeFetcherTest {

	private RowAndColumnPrivilegeFetcher delegate;
	private PartiallyCachingRowAndColumnPrivilegeFetcher fetcher;

	@Before
	public void setUp() {
		delegate = mock(RowAndColumnPrivilegeFetcher.class);
		fetcher = new PartiallyCachingRowAndColumnPrivilegeFetcher(delegate);
	}

	@Test
	public void fetchPrivilegeFiltersFor_SingleEntryType_NotCached() throws Exception {
		// given
		final CMClass entryType = mock(CMClass.class);

		// when
		fetcher.fetchPrivilegeFiltersFor(entryType);
		fetcher.fetchPrivilegeFiltersFor(entryType);

		// then
		verify(delegate, times(2)).fetchPrivilegeFiltersFor(eq(entryType));
	}

	@Test
	public void fetchAttributesPrivilegesFor_Cached() throws Exception {
		// given
		final CMClass entryType = mock(CMClass.class);

		// when
		fetcher.fetchAttributesPrivilegesFor(entryType);
		fetcher.fetchAttributesPrivilegesFor(entryType);

		// then
		verify(delegate, only()).fetchAttributesPrivilegesFor(eq(entryType));
	}

}
