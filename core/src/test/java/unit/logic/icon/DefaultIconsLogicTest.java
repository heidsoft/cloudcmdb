package unit.logic.icon;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.icon.DefaultIconsLogic;
import org.cmdbuild.logic.icon.Icon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Converter;

public class DefaultIconsLogicTest {

	private static interface ConverterDelegate {

		org.cmdbuild.data.store.icon.Icon doForward(Icon a);

		Icon doBackward(org.cmdbuild.data.store.icon.Icon b);

	}

	private Store<org.cmdbuild.data.store.icon.Icon> store;
	private ConverterDelegate converterDelegate;
	private DefaultIconsLogic underTest;

	@Before
	public void setUp() {
		store = mock(Store.class);
		converterDelegate = mock(ConverterDelegate.class);
		final Converter<Icon, org.cmdbuild.data.store.icon.Icon> converter = new Converter<Icon, org.cmdbuild.data.store.icon.Icon>() {

			@Override
			protected org.cmdbuild.data.store.icon.Icon doForward(final Icon a) {
				return converterDelegate.doForward(a);
			}

			@Override
			protected Icon doBackward(final org.cmdbuild.data.store.icon.Icon b) {
				return converterDelegate.doBackward(b);
			}

		};
		underTest = new DefaultIconsLogic(store, converter);
	}

	@Test(expected = NullPointerException.class)
	public void createThrowsExceptionWhenElementIsMissing() throws Exception {
		// when
		underTest.create(null);
	}

	@Test
	public void create() throws Exception {
		// given
		final Icon value = mock(Icon.class);
		final org.cmdbuild.data.store.icon.Icon convertedForward = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(convertedForward) //
				.when(converterDelegate).doForward(any(Icon.class));
		final Storable created = mock(Storable.class);
		doReturn(created) //
				.when(store).create(any(org.cmdbuild.data.store.icon.Icon.class));
		final org.cmdbuild.data.store.icon.Icon read = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(read) //
				.when(store).read(any(Storable.class));
		final Icon convertedBackward = mock(Icon.class);
		doReturn(convertedBackward) //
				.when(converterDelegate).doBackward(any(org.cmdbuild.data.store.icon.Icon.class));

		// when
		final Icon result = underTest.create(value);

		// then
		verify(converterDelegate).doForward(eq(value));
		verify(store).create(eq(convertedForward));
		verify(store).read(eq(created));
		verify(converterDelegate).doBackward(eq(read));
		verifyNoMoreInteractions(store, converterDelegate);

		assertThat(result, equalTo(convertedBackward));
	}

	@Test
	public void readAll() throws Exception {
		// given
		final org.cmdbuild.data.store.icon.Icon first = mock(org.cmdbuild.data.store.icon.Icon.class);
		final org.cmdbuild.data.store.icon.Icon second = mock(org.cmdbuild.data.store.icon.Icon.class);
		final org.cmdbuild.data.store.icon.Icon third = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(asList(first, second, third)) //
				.when(store).readAll();
		final Icon firstConverted = mock(Icon.class);
		final Icon secondConverted = mock(Icon.class);
		final Icon thirdConverted = mock(Icon.class);
		doReturn(firstConverted).doReturn(secondConverted).doReturn(thirdConverted) //
				.when(converterDelegate).doBackward(any(org.cmdbuild.data.store.icon.Icon.class));

		// when
		final Iterable<Icon> result = underTest.read();

		// then
		final ArgumentCaptor<org.cmdbuild.data.store.icon.Icon> captor = ArgumentCaptor
				.forClass(org.cmdbuild.data.store.icon.Icon.class);
		verify(store).readAll();
		verify(converterDelegate, times(3)).doBackward(captor.capture());
		verifyNoMoreInteractions(store, converterDelegate);

		assertThat(captor.getAllValues(), contains(first, second, third));
		assertThat(result, contains(firstConverted, secondConverted, thirdConverted));
	}

	@Test
	public void readThrowsExceptionWhenElementIsMissing() throws Exception {
		// when
		underTest.read(null);
	}

	@Test
	public void readReturnsEmptyWhenIconIsNotFound() throws Exception {
		// given
		final org.cmdbuild.data.store.icon.Icon first = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(1L) //
				.when(first).getId();
		final org.cmdbuild.data.store.icon.Icon second = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(2L) //
				.when(second).getId();
		final org.cmdbuild.data.store.icon.Icon third = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(3L) //
				.when(third).getId();
		doReturn(asList(first, second, third)) //
				.when(store).readAll();
		final Icon element = mock(Icon.class);
		doReturn(4L) //
				.when(element).getId();

		// when
		final Optional<Icon> result = underTest.read(element);

		// then
		verify(store).readAll();
		verifyNoMoreInteractions(store, converterDelegate);

		assertThat(result.isPresent(), equalTo(false));
	}

	@Test
	public void readSingleElement() throws Exception {
		// given
		final org.cmdbuild.data.store.icon.Icon first = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(1L) //
				.when(first).getId();
		final org.cmdbuild.data.store.icon.Icon second = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(2L) //
				.when(second).getId();
		final org.cmdbuild.data.store.icon.Icon third = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(3L) //
				.when(third).getId();
		doReturn(asList(first, second, third)) //
				.when(store).readAll();
		final Icon element = mock(Icon.class);
		doReturn(2L) //
				.when(element).getId();
		final Icon converted = mock(Icon.class);
		doReturn(converted) //
				.when(converterDelegate).doBackward(any(org.cmdbuild.data.store.icon.Icon.class));

		// when
		final Optional<Icon> result = underTest.read(element);

		// then
		verify(store).readAll();
		verify(converterDelegate).doBackward(eq(second));
		verifyNoMoreInteractions(store, converterDelegate);

		assertThat(result.get(), equalTo(converted));
	}

	@Test
	public void updateThrowsExceptionWhenElementIsMissing() throws Exception {
		// when
		underTest.update(null);
	}

	@Test
	public void updateDoesNothingWhenIconIsNotFound() throws Exception {
		// given
		final org.cmdbuild.data.store.icon.Icon first = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(1L) //
				.when(first).getId();
		final org.cmdbuild.data.store.icon.Icon second = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(2L) //
				.when(second).getId();
		final org.cmdbuild.data.store.icon.Icon third = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(3L) //
				.when(third).getId();
		doReturn(asList(first, second, third)) //
				.when(store).readAll();
		final Icon element = mock(Icon.class);
		doReturn(4L) //
				.when(element).getId();

		// when
		underTest.update(element);

		// then
		verify(store).readAll();
		verifyNoMoreInteractions(store, converterDelegate);
	}

	@Test
	public void update() throws Exception {
		// given
		final org.cmdbuild.data.store.icon.Icon first = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(1L) //
				.when(first).getId();
		final org.cmdbuild.data.store.icon.Icon second = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(2L) //
				.when(second).getId();
		final org.cmdbuild.data.store.icon.Icon third = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(3L) //
				.when(third).getId();
		doReturn(asList(first, second, third)) //
				.when(store).readAll();
		final Icon element = mock(Icon.class);
		doReturn(2L) //
				.when(element).getId();
		final Icon convertedBackward = mock(Icon.class);
		doReturn(convertedBackward) //
				.when(converterDelegate).doBackward(any(org.cmdbuild.data.store.icon.Icon.class));
		final org.cmdbuild.data.store.icon.Icon convertedForward = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(convertedForward) //
				.when(converterDelegate).doForward(any(Icon.class));

		// when
		underTest.update(element);

		// then
		verify(store).readAll();
		verify(converterDelegate).doForward(eq(element));
		verify(store).update(eq(convertedForward));
		verifyNoMoreInteractions(store, converterDelegate);
	}

	@Test
	public void deleteThrowsExceptionWhenElementIsMissing() throws Exception {
		// when
		underTest.delete(null);
	}

	@Test
	public void deleteDoesNothingWhenIconIsNotFound() throws Exception {
		// given
		final org.cmdbuild.data.store.icon.Icon first = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(1L) //
				.when(first).getId();
		final org.cmdbuild.data.store.icon.Icon second = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(2L) //
				.when(second).getId();
		final org.cmdbuild.data.store.icon.Icon third = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(3L) //
				.when(third).getId();
		doReturn(asList(first, second, third)) //
				.when(store).readAll();
		final Icon element = mock(Icon.class);
		doReturn(4L) //
				.when(element).getId();

		// when
		underTest.delete(element);

		// then
		verify(store).readAll();
		verifyNoMoreInteractions(store, converterDelegate);
	}

	@Test
	public void delete() throws Exception {
		// given
		final org.cmdbuild.data.store.icon.Icon first = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(1L) //
				.when(first).getId();
		final org.cmdbuild.data.store.icon.Icon second = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(2L) //
				.when(second).getId();
		final org.cmdbuild.data.store.icon.Icon third = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(3L) //
				.when(third).getId();
		doReturn(asList(first, second, third)) //
				.when(store).readAll();
		final Icon element = mock(Icon.class);
		doReturn(2L) //
				.when(element).getId();
		final Icon convertedBackward = mock(Icon.class);
		doReturn(convertedBackward) //
				.when(converterDelegate).doBackward(any(org.cmdbuild.data.store.icon.Icon.class));
		final org.cmdbuild.data.store.icon.Icon convertedForward = mock(org.cmdbuild.data.store.icon.Icon.class);
		doReturn(convertedForward) //
				.when(converterDelegate).doForward(any(Icon.class));

		// when
		underTest.delete(element);

		// then
		verify(store).readAll();
		verify(converterDelegate).doForward(eq(element));
		verify(store).delete(eq(convertedForward));
		verifyNoMoreInteractions(store, converterDelegate);
	}

}
