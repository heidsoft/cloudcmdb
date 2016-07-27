package unit.logic.custompages;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.custompage.DBCustomPage;
import org.cmdbuild.logic.custompages.CustomPage;
import org.cmdbuild.logic.custompages.DefaultCustomPagesLogic;
import org.cmdbuild.logic.custompages.DefaultCustomPagesLogic.AccessControlHelper;
import org.cmdbuild.logic.custompages.DefaultCustomPagesLogic.Converter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DefaultCustomPagesLogicTest {

	private Store<DBCustomPage> store;
	private Converter converter;
	private AccessControlHelper accessControlHelper;
	private DefaultCustomPagesLogic underTest;

	@Before
	public void setUp() throws Exception {
		store = mock(Store.class);
		converter = mock(Converter.class);
		accessControlHelper = mock(AccessControlHelper.class);
		underTest = new DefaultCustomPagesLogic(store, converter, accessControlHelper);
	}

	@Test
	public void customPagesReadWithNoAccessControl() throws Exception {
		// given
		final DBCustomPage foo = storableCustomPage(1L);
		final DBCustomPage bar = storableCustomPage(2L);
		final DBCustomPage baz = storableCustomPage(3L);
		doReturn(asList(foo, bar, baz)) //
				.when(store).readAll();
		final CustomPage _foo = customPage(1L);
		final CustomPage _bar = customPage(2L);
		final CustomPage _baz = customPage(3L);
		doReturn(_foo).doReturn(_bar).doReturn(_baz) //
				.when(converter).toLogic(any(DBCustomPage.class));

		// when
		final Iterable<CustomPage> output = from((underTest.read())) //
				// defensive copy
				.toList();

		// then
		assertThat(size(output), equalTo(3));
		assertThat(get(output, 0).getId(), equalTo(1L));
		assertThat(get(output, 1).getId(), equalTo(2L));
		assertThat(get(output, 2).getId(), equalTo(3L));

		final ArgumentCaptor<DBCustomPage> captor = ArgumentCaptor.forClass(DBCustomPage.class);
		verify(store).readAll();
		verify(converter, times(3)).toLogic(captor.capture());
		verifyNoMoreInteractions(store, converter, accessControlHelper);

		assertThat(captor.getAllValues().get(0), equalTo(foo));
		assertThat(captor.getAllValues().get(1), equalTo(bar));
		assertThat(captor.getAllValues().get(2), equalTo(baz));
	}

	@Test
	public void customPagesReadWithAccessControl() throws Exception {
		// given
		final DBCustomPage foo = storableCustomPage(1L);
		final DBCustomPage bar = storableCustomPage(2L);
		final DBCustomPage baz = storableCustomPage(3L);
		doReturn(asList(foo, bar, baz)) //
				.when(store).readAll();
		final CustomPage _foo = customPage(1L);
		final CustomPage _bar = customPage(2L);
		final CustomPage _baz = customPage(3L);
		doReturn(_foo).doReturn(_bar).doReturn(_baz) //
				.when(converter).toLogic(any(DBCustomPage.class));
		doReturn(true).doReturn(false).doReturn(true) //
				.when(accessControlHelper).isAccessible(any(CustomPage.class));

		// when
		final Iterable<CustomPage> output = from((underTest.readForCurrentUser())) //
				// defensive copy
				.toList();

		// then
		assertThat(size(output), equalTo(2));
		assertThat(get(output, 0).getId(), equalTo(1L));
		assertThat(get(output, 1).getId(), equalTo(3L));

		final ArgumentCaptor<DBCustomPage> captor = ArgumentCaptor.forClass(DBCustomPage.class);
		final ArgumentCaptor<CustomPage> captor2 = ArgumentCaptor.forClass(CustomPage.class);
		verify(store).readAll();
		verify(converter, times(3)).toLogic(captor.capture());
		verify(accessControlHelper, times(3)).isAccessible(captor2.capture());
		verifyNoMoreInteractions(store, converter, accessControlHelper);

		assertThat(captor.getAllValues().get(0), equalTo(foo));
		assertThat(captor.getAllValues().get(1), equalTo(bar));
		assertThat(captor.getAllValues().get(2), equalTo(baz));

		assertThat(captor2.getAllValues().get(0), equalTo(_foo));
		assertThat(captor2.getAllValues().get(1), equalTo(_bar));
		assertThat(captor2.getAllValues().get(2), equalTo(_baz));
	}

	@Test
	public void customPageRead() throws Exception {
		// given
		final DBCustomPage readed = storableCustomPage(1L);
		doReturn(readed) //
				.when(store).read(any(Storable.class));
		final CustomPage converted = customPage(1L);
		doReturn(converted) //
				.when(converter).toLogic(any(DBCustomPage.class));

		// when
		final CustomPage output = underTest.read(42L);

		// then
		assertThat(output, equalTo(converted));

		final ArgumentCaptor<DBCustomPage> captor = ArgumentCaptor.forClass(DBCustomPage.class);
		verify(store).read(captor.capture());
		verify(converter).toLogic(eq(readed));
		verifyNoMoreInteractions(store, converter, accessControlHelper);

		final DBCustomPage captured = captor.getValue();
		assertThat(captured.getIdentifier(), equalTo("42"));
		assertThat(captured.getId(), equalTo(42L));
	}

	/*
	 * Utilities
	 */

	private CustomPage customPage(final Long id) {
		return new CustomPage() {

			@Override
			public Long getId() {
				return id;
			}

			@Override
			public String getName() {
				return getId().toString();
			}

			@Override
			public String getDescription() {
				return getId().toString();
			}

			@Override
			public String toString() {
				return getId().toString();
			}

		};
	}

	private DBCustomPage storableCustomPage(final Long id) {
		return new DBCustomPage() {

			@Override
			public String getIdentifier() {
				return getId().toString();
			}

			@Override
			public Long getId() {
				return id;
			}

			@Override
			public String getName() {
				return getIdentifier();
			}

			@Override
			public String getDescription() {
				return getIdentifier();
			}

			@Override
			public String toString() {
				return getIdentifier();
			}

		};
	}

}
