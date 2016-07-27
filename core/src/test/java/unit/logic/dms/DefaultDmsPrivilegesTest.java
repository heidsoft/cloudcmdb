package unit.logic.dms;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic.DefaultDmsPrivileges;
import org.junit.Before;
import org.junit.Test;

public class DefaultDmsPrivilegesTest {

	private CMDataView dataView;
	private PrivilegeContext privilegeContext;
	private DefaultDmsPrivileges underTest;

	@Before
	public void setUp() throws Exception {
		dataView = mock(CMDataView.class);
		privilegeContext = mock(PrivilegeContext.class);
		underTest = new DefaultDmsPrivileges(dataView, privilegeContext);
	}

	@Test
	public void notReadableWhenClassNameIsNull() throws Exception {
		// when
		final boolean output = underTest.readable(null);

		// then
		assertThat(output, equalTo(false));
		verifyNoMoreInteractions(dataView, privilegeContext);
	}

	@Test
	public void notReadableWhenClassIsMissing() throws Exception {
		// given
		doReturn(null) //
				.when(dataView).findClass(anyString());

		// when
		final boolean output = underTest.readable("foo");

		// then
		assertThat(output, equalTo(false));

		verify(dataView).findClass("foo");
		verifyNoMoreInteractions(dataView, privilegeContext);
	}

	@Test
	public void notReadableWhenNotReadableForCurrentPrivileges() throws Exception {
		// given
		final CMClass element = mock(CMClass.class);
		doReturn(element) //
				.when(dataView).findClass(anyString());
		doReturn(false) //
				.when(privilegeContext).hasReadAccess(any(CMPrivilegedObject.class));

		// when
		final boolean output = underTest.readable("foo");

		// then
		assertThat(output, equalTo(false));

		verify(dataView).findClass("foo");
		verify(privilegeContext).hasReadAccess(element);
		verifyNoMoreInteractions(dataView, privilegeContext);
	}

	@Test
	public void readableWhenReadableForCurrentPrivileges() throws Exception {
		// given
		final CMClass element = mock(CMClass.class);
		doReturn(element) //
				.when(dataView).findClass(anyString());
		doReturn(true) //
				.when(privilegeContext).hasReadAccess(any(CMPrivilegedObject.class));

		// when
		final boolean output = underTest.readable("foo");

		// then
		assertThat(output, equalTo(true));

		verify(dataView).findClass("foo");
		verify(privilegeContext).hasReadAccess(element);
		verifyNoMoreInteractions(dataView, privilegeContext);
	}

	@Test
	public void notWritableWhenClassNameIsNull() throws Exception {
		// when
		final boolean output = underTest.writable(null);

		// then
		assertThat(output, equalTo(false));
		verifyNoMoreInteractions(dataView, privilegeContext);
	}

	@Test
	public void notWritableWhenClassIsMissing() throws Exception {
		// given
		doReturn(null) //
				.when(dataView).findClass(anyString());

		// when
		final boolean output = underTest.writable("foo");

		// then
		assertThat(output, equalTo(false));

		verify(dataView).findClass("foo");
		verifyNoMoreInteractions(dataView, privilegeContext);
	}

	@Test
	public void notWritableWhenNotWritableForCurrentPrivilegesAndClassIsNotProcessClass() throws Exception {
		// given
		final CMClass activityClass = mock(CMClass.class);
		doReturn(false) //
				.when(activityClass).isAncestorOf(any(CMClass.class));
		doReturn(activityClass) //
				.when(dataView).getActivityClass();
		final CMClass element = mock(CMClass.class);
		doReturn(element) //
				.when(dataView).findClass(anyString());
		doReturn(false) //
				.when(privilegeContext).hasWriteAccess(any(CMPrivilegedObject.class));

		// when
		final boolean output = underTest.writable("foo");

		// then
		assertThat(output, equalTo(false));

		verify(dataView).findClass("foo");
		verify(privilegeContext).hasWriteAccess(element);
		verify(dataView).getActivityClass();
		verify(activityClass).isAncestorOf(element);
		verifyNoMoreInteractions(dataView, privilegeContext, activityClass);
	}

	@Test
	public void writableWhenWritableForCurrentPrivileges() throws Exception {
		// given
		final CMClass element = mock(CMClass.class);
		doReturn(element) //
				.when(dataView).findClass(anyString());
		doReturn(true) //
				.when(privilegeContext).hasWriteAccess(any(CMPrivilegedObject.class));

		// when
		final boolean output = underTest.writable("foo");

		// then
		assertThat(output, equalTo(true));

		verify(dataView).findClass("foo");
		verify(privilegeContext).hasWriteAccess(element);
		verifyNoMoreInteractions(dataView, privilegeContext);
	}

	@Test
	public void writableWhenClassIsProcessClassEvenIfNotWritableForCurrentPrivileges() throws Exception {
		// given
		final CMClass activityClass = mock(CMClass.class);
		doReturn(true) //
				.when(activityClass).isAncestorOf(any(CMClass.class));
		doReturn(activityClass) //
				.when(dataView).getActivityClass();
		final CMClass element = mock(CMClass.class);
		doReturn(element) //
				.when(dataView).findClass(anyString());
		doReturn(false) //
				.when(privilegeContext).hasWriteAccess(any(CMPrivilegedObject.class));

		// when
		final boolean output = underTest.writable("foo");

		// then
		assertThat(output, equalTo(true));

		verify(dataView).findClass("foo");
		verify(privilegeContext).hasWriteAccess(element);
		verify(dataView).getActivityClass();
		verify(activityClass).isAncestorOf(element);
		verifyNoMoreInteractions(dataView, privilegeContext, activityClass);
	}

}
