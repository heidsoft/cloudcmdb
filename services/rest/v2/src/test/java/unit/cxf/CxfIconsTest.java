package unit.cxf;

import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v2.model.Models.newIcon;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
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

import org.cmdbuild.logic.icon.IconsLogic;
import org.cmdbuild.service.rest.v2.cxf.CxfIcons;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.model.Icon;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Converter;

public class CxfIconsTest {

	private static interface ConverterDelegate {

		org.cmdbuild.logic.icon.Icon doForward(Icon a);

		Icon doBackward(org.cmdbuild.logic.icon.Icon b);

	}

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private ErrorHandler errorHandler;
	private IconsLogic iconsLogic;
	private ConverterDelegate converterDelegate;
	private CxfIcons underTest;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		iconsLogic = mock(IconsLogic.class);
		converterDelegate = mock(ConverterDelegate.class);
		final Converter<Icon, org.cmdbuild.logic.icon.Icon> converter = new Converter<Icon, org.cmdbuild.logic.icon.Icon>() {

			@Override
			protected org.cmdbuild.logic.icon.Icon doForward(final Icon a) {
				return converterDelegate.doForward(a);
			}

			@Override
			protected Icon doBackward(final org.cmdbuild.logic.icon.Icon b) {
				return converterDelegate.doBackward(b);
			}

		};
		underTest = new CxfIcons(errorHandler, iconsLogic, converter);
	}

	@Test(expected = NullPointerException.class)
	public void createWithNullIconThrowsException() throws Exception {
		// when
		underTest.create(null);
	}

	@Test
	public void create() throws Exception {
		// given
		final Icon inputIcon = newIcon().build();
		final org.cmdbuild.logic.icon.Icon elementFromIcon = mock(org.cmdbuild.logic.icon.Icon.class);
		doReturn(elementFromIcon) //
				.when(converterDelegate).doForward(any(Icon.class));
		final org.cmdbuild.logic.icon.Icon elementFromLogic = mock(org.cmdbuild.logic.icon.Icon.class);
		doReturn(elementFromLogic) //
				.when(iconsLogic).create(any(org.cmdbuild.logic.icon.Icon.class));
		final Icon outputIcon = newIcon().build();
		doReturn(outputIcon) //
				.when(converterDelegate).doBackward(any(org.cmdbuild.logic.icon.Icon.class));

		// when
		final ResponseSingle<Icon> response = underTest.create(inputIcon);

		// then
		verify(converterDelegate).doForward(eq(inputIcon));
		verify(iconsLogic).create(eq(elementFromIcon));
		verify(converterDelegate).doBackward(eq(elementFromLogic));
		verifyNoMoreInteractions(errorHandler, iconsLogic, converterDelegate);

		assertThat(response,
				equalTo(newResponseSingle(Icon.class) //
						.withElement(outputIcon) //
						.build()));
	}

	@Test
	public void read() throws Exception {
		// given
		final org.cmdbuild.logic.icon.Icon first = mock(org.cmdbuild.logic.icon.Icon.class);
		final org.cmdbuild.logic.icon.Icon second = mock(org.cmdbuild.logic.icon.Icon.class);
		final org.cmdbuild.logic.icon.Icon third = mock(org.cmdbuild.logic.icon.Icon.class);
		doReturn(asList(first, second, third)) //
				.when(iconsLogic).read();
		final Icon _first = newIcon().build();
		final Icon _second = newIcon().build();
		final Icon _third = newIcon().build();
		doReturn(_first).doReturn(_second).doReturn(_third) //
				.when(converterDelegate).doBackward(any(org.cmdbuild.logic.icon.Icon.class));

		// when
		final ResponseMultiple<Icon> response = underTest.read();

		// then
		verify(iconsLogic).read();
		final ArgumentCaptor<org.cmdbuild.logic.icon.Icon> captor = ArgumentCaptor
				.forClass(org.cmdbuild.logic.icon.Icon.class);
		verify(converterDelegate, times(3)).doBackward(captor.capture());
		assertThat(captor.getAllValues(), contains(first, second, third));
		verifyNoMoreInteractions(errorHandler, iconsLogic, converterDelegate);

		assertThat(response,
				equalTo(newResponseMultiple(Icon.class) //
						.withElements(asList(_first, _second, _third)) //
						.withMetadata(newMetadata() //
								.withTotal(3) //
								.build())
						.build()));
	}

	@Test(expected = NullPointerException.class)
	public void readWithNullIdThrowsException() throws Exception {
		// when
		underTest.read(null);
	}

	@Test
	public void readMissingElementInvokesErrorHandler() throws Exception {
		// given
		doReturn(Optional.empty()) //
				.when(iconsLogic).read(any(org.cmdbuild.logic.icon.Icon.class));

		// when
		underTest.read(42L);

		// then
		final ArgumentCaptor<org.cmdbuild.logic.icon.Icon> captor = ArgumentCaptor
				.forClass(org.cmdbuild.logic.icon.Icon.class);
		verify(iconsLogic).read(captor.capture());
		verify(errorHandler).missingIcon(eq(42L));
		verifyNoMoreInteractions(errorHandler, iconsLogic, converterDelegate);
	}

	@Test
	public void readSingleIcon() throws Exception {
		// given
		final org.cmdbuild.logic.icon.Icon element = mock(org.cmdbuild.logic.icon.Icon.class);
		doReturn(Optional.of(element)) //
				.when(iconsLogic).read(any(org.cmdbuild.logic.icon.Icon.class));
		final Icon outputIcon = newIcon().build();
		doReturn(outputIcon) //
				.when(converterDelegate).doBackward(any(org.cmdbuild.logic.icon.Icon.class));

		// when
		final ResponseSingle<Icon> response = underTest.read(42L);

		// then
		final ArgumentCaptor<org.cmdbuild.logic.icon.Icon> captor = ArgumentCaptor
				.forClass(org.cmdbuild.logic.icon.Icon.class);
		verify(iconsLogic).read(captor.capture());
		verify(converterDelegate).doBackward(eq(element));
		verifyNoMoreInteractions(errorHandler, iconsLogic, converterDelegate);

		assertThat(captor.getValue().getId(), equalTo(42L));
		assertThat(response,
				equalTo(newResponseSingle(Icon.class) //
						.withElement(outputIcon) //
						.build()));
	}

	@Test(expected = NullPointerException.class)
	public void updateWithNullIdThrowsException() throws Exception {
		// given
		final Icon icon = newIcon().build();

		// when
		underTest.update(null, icon);
	}

	@Test(expected = NullPointerException.class)
	public void updateWithNullIconThrowsException() throws Exception {
		// when
		underTest.update(42L, null);
	}

	@Test
	public void updateWithMissingElementInvokesErrorHandler() throws Exception {
		// given
		final Icon icon = newIcon().build();
		doReturn(Optional.empty()) //
				.when(iconsLogic).read(any(org.cmdbuild.logic.icon.Icon.class));

		// when
		underTest.update(42L, icon);

		// then
		final ArgumentCaptor<org.cmdbuild.logic.icon.Icon> captor = ArgumentCaptor
				.forClass(org.cmdbuild.logic.icon.Icon.class);
		verify(iconsLogic).read(captor.capture());
		verify(errorHandler).missingIcon(eq(42L));
		verifyNoMoreInteractions(errorHandler, iconsLogic, converterDelegate);

		assertThat(captor.getValue().getId(), equalTo(42L));
	}

	@Test
	public void update() throws Exception {
		// given
		final Icon icon = newIcon().build();
		final org.cmdbuild.logic.icon.Icon converted = mock(org.cmdbuild.logic.icon.Icon.class);
		doReturn(converted) //
				.when(converterDelegate).doForward(any(Icon.class));
		final org.cmdbuild.logic.icon.Icon read = mock(org.cmdbuild.logic.icon.Icon.class);
		doReturn(Optional.of(read)) //
				.when(iconsLogic).read(any(org.cmdbuild.logic.icon.Icon.class));

		// when
		underTest.update(42L, icon);

		// then
		final ArgumentCaptor<org.cmdbuild.logic.icon.Icon> captor = ArgumentCaptor
				.forClass(org.cmdbuild.logic.icon.Icon.class);
		verify(iconsLogic).read(captor.capture());
		verify(iconsLogic).update(captor.capture());
		verify(converterDelegate).doForward(eq(icon));
		verifyNoMoreInteractions(errorHandler, iconsLogic, converterDelegate);

		assertThat(captor.getAllValues().get(0).getId(), equalTo(42L));
	}

	@Test(expected = NullPointerException.class)
	public void deleteWithNullIdThrowsException() throws Exception {
		// when
		underTest.delete(null);
	}

	@Test
	public void deleteWithMissingElementInvokesErrorHandler() throws Exception {
		// given
		doReturn(Optional.empty()) //
				.when(iconsLogic).read(any(org.cmdbuild.logic.icon.Icon.class));

		// when
		underTest.delete(42L);

		// then
		final ArgumentCaptor<org.cmdbuild.logic.icon.Icon> captor = ArgumentCaptor
				.forClass(org.cmdbuild.logic.icon.Icon.class);
		verify(iconsLogic).read(captor.capture());
		verify(errorHandler).missingIcon(eq(42L));
		verifyNoMoreInteractions(errorHandler, iconsLogic, converterDelegate);

		assertThat(captor.getValue().getId(), equalTo(42L));
	}

	@Test
	public void delete() throws Exception {
		// given
		final org.cmdbuild.logic.icon.Icon element = mock(org.cmdbuild.logic.icon.Icon.class);
		doReturn(Optional.of(element)) //
				.when(iconsLogic).read(any(org.cmdbuild.logic.icon.Icon.class));

		// when
		underTest.delete(42L);

		// then
		final ArgumentCaptor<org.cmdbuild.logic.icon.Icon> captor = ArgumentCaptor
				.forClass(org.cmdbuild.logic.icon.Icon.class);
		verify(iconsLogic).read(captor.capture());
		verify(iconsLogic).delete(captor.capture());
		verifyNoMoreInteractions(errorHandler, iconsLogic, converterDelegate);

		assertThat(captor.getAllValues().get(0).getId(), equalTo(42L));
		assertThat(captor.getAllValues().get(1), equalTo(element));
	}

}
