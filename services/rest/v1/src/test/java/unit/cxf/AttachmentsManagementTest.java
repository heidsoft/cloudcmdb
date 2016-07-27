package unit.cxf;

import static java.util.Collections.emptyList;
import static org.cmdbuild.service.rest.v1.model.Models.newAttachment;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.io.input.NullInputStream;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.service.rest.v1.cxf.AttachmentsManagement;
import org.cmdbuild.service.rest.v1.model.Attachment;
import org.junit.Before;
import org.junit.Test;

public class AttachmentsManagementTest {

	protected static final Iterable<MetadataGroupDefinition> NO_METADATA_GROUP_DEFINITION = emptyList();

	private static final DocumentTypeDefinition NO_METADATA = new DocumentTypeDefinition() {

		@Override
		public String getName() {
			return "dummy " + DocumentTypeDefinition.class;
		};

		@Override
		public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
			return NO_METADATA_GROUP_DEFINITION;
		};

	};

	private DmsLogic dmsLogic;
	private UserStore userStore;
	private AttachmentsManagement attachmentsManagement;

	@Before
	public void setUp() throws Exception {
		dmsLogic = mock(DmsLogic.class);
		userStore = mock(UserStore.class);
		attachmentsManagement = new AttachmentsManagement(dmsLogic, userStore);

		final AuthenticatedUser authUser = mock(AuthenticatedUser.class);
		doReturn("dummy user") //
				.when(authUser).getUsername();
		final OperationUser operationUser = new OperationUser(authUser, new NullPrivilegeContext(), new NullGroup());
		doReturn(operationUser) //
				.when(userStore).getUser();
	}

	@Test
	public void logicCalledOnCreateWithBothAttachmentAndFile() throws Exception {
		// given
		final Attachment attachment = newAttachment() //
				.withCategory("the category") //
				.withDescription("the description") //
				.build();
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn("file name") //
				.when(dataSource).getName();
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		attachmentsManagement.create("foo", 123L, "bar", attachment, dataHandler);

		// then
		verify(userStore).getUser();
		verify(dmsLogic).getCategoryDefinition(eq("the category"));
		verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("bar"), eq("the category"),
				eq("the description"), any(Iterable.class));
		verifyNoMoreInteractions(userStore, dmsLogic);
	}

	@Test
	public void logicCalledOnCreateWithFileOnly() throws Exception {
		// given
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn("file name") //
				.when(dataSource).getName();
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		attachmentsManagement.create("foo", 123L, "bar", null, dataHandler);

		// then
		verify(userStore).getUser();
		verify(dmsLogic).getCategoryDefinition(isNull(String.class));
		verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("bar"),
				isNull(String.class), isNull(String.class), any(Iterable.class));
		verifyNoMoreInteractions(userStore, dmsLogic);
	}

	@Test
	public void logicCalledOnReadAll() throws Exception {
		// when
		attachmentsManagement.search("foo", 123L);

		// then
		verify(dmsLogic).search(eq("foo"), eq(123L));
		verifyNoMoreInteractions(dmsLogic);
	}

	@Test
	public void logicCalledOnRead() throws Exception {
		// when
		attachmentsManagement.download("foo", 123L, "bar");

		// then
		verify(dmsLogic).download(eq("foo"), eq(123L), eq("bar"));
		verifyNoMoreInteractions(dmsLogic);
	}

	@Test
	public void logicCalledOnUpdateWithBothAttachmentAndFile() throws Exception {
		// given
		final Attachment attachment = newAttachment() //
				.withCategory("the new category") //
				.withDescription("the new description") //
				.build();
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		attachmentsManagement.update("foo", 123L, "bar", attachment, dataHandler);

		// then
		verify(userStore).getUser();
		verify(dmsLogic).getCategoryDefinition(eq("the new category"));
		verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("bar"),
				eq("the new category"), eq("the new description"), any(Iterable.class));
		verifyNoMoreInteractions(userStore, dmsLogic);
	}

	@Test
	public void logicCalledOnUpdateWithFileOnly() throws Exception {
		// given
		final InputStream inputStream = new NullInputStream(1024);
		final DataSource dataSource = mock(DataSource.class);
		doReturn(inputStream) //
				.when(dataSource).getInputStream();
		final DataHandler dataHandler = new DataHandler(dataSource);
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		attachmentsManagement.update("foo", 123L, "bar", null, dataHandler);

		// then
		verify(userStore).getUser();
		verify(dmsLogic).getCategoryDefinition(isNull(String.class));
		verify(dmsLogic).upload(eq("dummy user"), eq("foo"), eq(123L), same(inputStream), eq("bar"),
				isNull(String.class), isNull(String.class), any(Iterable.class));
		verifyNoMoreInteractions(userStore, dmsLogic);
	}

	@Test
	public void logicCalledOnUpdateWithAttachmentOnly() throws Exception {
		// given
		final Attachment attachment = newAttachment() //
				.withCategory("the new category") //
				.withDescription("the new description") //
				.build();
		doReturn(NO_METADATA) //
				.when(dmsLogic).getCategoryDefinition(anyString());

		// when
		attachmentsManagement.update("foo", 123L, "bar", attachment, null);

		// then
		verify(dmsLogic).getCategoryDefinition(eq("the new category"));
		verify(dmsLogic).updateDescriptionAndMetadata(eq("dummy user"), eq("foo"), eq(123L), eq("bar"),
				eq("the new category"), eq("the new description"), any(Iterable.class));
		verifyNoMoreInteractions(dmsLogic);
	}

	@Test
	public void logicCalledOnDelete() throws Exception {
		// when
		attachmentsManagement.delete("foo", 123L, "bar");

		// then
		verify(dmsLogic).delete(eq("foo"), eq(123L), eq("bar"));
		verifyNoMoreInteractions(dmsLogic);
	}

}
