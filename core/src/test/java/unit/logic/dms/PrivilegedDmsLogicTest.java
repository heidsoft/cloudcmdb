package unit.logic.dms;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic.DmsPrivileges;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PrivilegedDmsLogicTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private DmsLogic delegate;
	private DmsPrivileges dmsPrivileges;
	private PrivilegedDmsLogic underTest;

	@Before
	public void setUp() throws Exception {
		delegate = mock(DmsLogic.class);
		dmsPrivileges = mock(DmsPrivileges.class);
		underTest = new PrivilegedDmsLogic(delegate, dmsPrivileges);
	}

	@Test(expected = AuthException.class)
	public void noReadPrivilegeWhenSearching() throws Exception {
		// given
		doReturn(false).when(dmsPrivileges).readable(anyString());

		// when
		underTest.search("foo", 42L);
	}

	@Test
	public void readPrivilegeWhenSearching() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).readable(anyString());

		// when
		underTest.search("foo", 42L);

		// then
		verify(dmsPrivileges).readable("foo");
		verify(delegate).search("foo", 42L);
		verifyNoMoreInteractions(delegate, dmsPrivileges);
	}

	@Test(expected = AuthException.class)
	public void noReadPrivilegeWhenSearchingSpecificFile() throws Exception {
		// given
		doReturn(false).when(dmsPrivileges).readable(anyString());

		// when
		underTest.search("foo", 42L, "bar");
	}

	@Test
	public void readPrivilegeWhenSearchingSpecificFile() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).readable(anyString());

		// when
		underTest.search("foo", 42L, "bar");

		// then
		verify(dmsPrivileges).readable("foo");
		verify(delegate).search("foo", 42L, "bar");
		verifyNoMoreInteractions(delegate, dmsPrivileges);
	}

	@Test(expected = AuthException.class)
	public void noWritePrivilegeWhenUploading() throws Exception {
		// given
		doReturn(false).when(dmsPrivileges).writable(anyString());
		final InputStream inputStream = new ByteArrayInputStream(new byte[0]);
		final Iterable<MetadataGroup> metadataGroups = asList();

		// when
		underTest.upload("the author", "foo", 42L, inputStream, "the filename", "the category", "the description",
				metadataGroups);
	}

	@Test
	public void writePrivilegeWhenUploading() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).writable(anyString());
		final InputStream inputStream = new ByteArrayInputStream(new byte[0]);
		final Iterable<MetadataGroup> metadataGroups = asList();

		// when
		underTest.upload("the author", "foo", 42L, inputStream, "the filename", "the category", "the description",
				metadataGroups);

		// then
		verify(dmsPrivileges).writable("foo");
		verify(delegate).upload("the author", "foo", 42L, inputStream, "the filename", "the category",
				"the description", metadataGroups);
		verifyNoMoreInteractions(delegate, dmsPrivileges);
	}

	@Test(expected = AuthException.class)
	public void noWritePrivilegeWhenDeleting() throws Exception {
		// given
		doReturn(false).when(dmsPrivileges).writable(anyString());

		// when
		underTest.delete("foo", 42L, "the filename");
	}

	@Test
	public void writePrivilegeWhenDeleting() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).writable(anyString());

		// when
		underTest.delete("foo", 42L, "the filename");

		// then
		verify(dmsPrivileges).writable("foo");
		verify(delegate).delete("foo", 42L, "the filename");
		verifyNoMoreInteractions(delegate, dmsPrivileges);
	}

	@Test(expected = AuthException.class)
	public void noWritePrivilegeWhenUpdatingMetadata() throws Exception {
		// given
		doReturn(false).when(dmsPrivileges).writable(anyString());
		final Iterable<MetadataGroup> metadataGroups = asList();

		// when
		underTest.updateDescriptionAndMetadata("dummy user", "foo", 42L, "the filename", "the category", "the description",
				metadataGroups);
	}

	@Test
	public void writePrivilegeWhenUpdatingMetadata() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).writable(anyString());
		final Iterable<MetadataGroup> metadataGroups = asList();

		// when
		underTest.updateDescriptionAndMetadata("dummy user", "foo", 42L, "the filename", "the category", "the description",
				metadataGroups);

		// then
		verify(dmsPrivileges).writable("foo");
		verify(delegate).updateDescriptionAndMetadata("dummy user", "foo", 42L, "the filename", "the category", "the description",
				metadataGroups);
		verifyNoMoreInteractions(delegate, dmsPrivileges);
	}

	@Test(expected = AuthException.class)
	public void noReadPrivilegeOnSourceWhenCopying() throws Exception {
		// given
		doReturn(false).when(dmsPrivileges).readable("foo");
		doReturn(true).when(dmsPrivileges).writable("bar");

		// when
		try {
			underTest.copy("foo", 42L, "the filename", "bar", 24L);
		} catch (final AuthException e) {
			// then
			verify(dmsPrivileges).readable("foo");

			throw e;
		}
	}

	@Test(expected = AuthException.class)
	public void noWritePrivilegeOnDestinationWhenCopying() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).readable("foo");
		doReturn(false).when(dmsPrivileges).writable("bar");

		// when
		try {
			underTest.copy("foo", 42L, "the filename", "bar", 24L);
		} catch (final AuthException e) {
			// then
			verify(dmsPrivileges).readable("foo");
			verify(dmsPrivileges).writable("bar");

			throw e;
		}
	}

	@Test
	public void readPrivilegeOnSourceAndWritePrivilegeOnDestinationWhenCopying() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).readable("foo");
		doReturn(true).when(dmsPrivileges).writable("bar");

		// when
		underTest.copy("foo", 42L, "the filename", "bar", 24L);

		// then
		verify(dmsPrivileges).readable("foo");
		verify(dmsPrivileges).writable("bar");
		verify(delegate).copy("foo", 42L, "the filename", "bar", 24L);
		verifyNoMoreInteractions(delegate, dmsPrivileges);
	}

	@Test(expected = AuthException.class)
	public void noReadPrivilegeOnSourceWhenMoving() throws Exception {
		// given
		doReturn(false).when(dmsPrivileges).readable("foo");
		doReturn(true).when(dmsPrivileges).writable("bar");

		// when
		try {
			underTest.move("foo", 42L, "the filename", "bar", 24L);
		} catch (final AuthException e) {
			// then
			verify(dmsPrivileges).readable("foo");

			throw e;
		}
	}

	@Test(expected = AuthException.class)
	public void noWritePrivilegeOnDestinationWhenMoving() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).readable("foo");
		doReturn(false).when(dmsPrivileges).writable("bar");

		// when
		try {
			underTest.move("foo", 42L, "the filename", "bar", 24L);
		} catch (final AuthException e) {
			// then
			verify(dmsPrivileges).readable("foo");
			verify(dmsPrivileges).writable("bar");

			throw e;
		}
	}

	@Test
	public void readPrivilegeOnSourceAndWritePrivilegeOnDestinationWhenMoving() throws Exception {
		// given
		doReturn(true).when(dmsPrivileges).readable("foo");
		doReturn(true).when(dmsPrivileges).writable("bar");

		// when
		underTest.move("foo", 42L, "the filename", "bar", 24L);

		// then
		verify(dmsPrivileges).readable("foo");
		verify(dmsPrivileges).writable("bar");
		verify(delegate).move("foo", 42L, "the filename", "bar", 24L);
		verifyNoMoreInteractions(delegate, dmsPrivileges);
	}

}
