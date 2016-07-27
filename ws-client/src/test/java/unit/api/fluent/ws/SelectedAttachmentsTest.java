package unit.api.fluent.ws;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.cmdbuild.api.fluent.Attachment;
import org.cmdbuild.api.fluent.AttachmentDescriptor;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.SelectedAttachments;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class SelectedAttachmentsTest extends AbstractWsFluentApiTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private SelectedAttachments selectedAttachments;

	private static org.cmdbuild.services.soap.Attachment FOO;
	private static org.cmdbuild.services.soap.Attachment BAR;
	private static org.cmdbuild.services.soap.Attachment BAZ;

	@Before
	public void setUp() throws Exception {
		FOO = soapAttachment("foo", "this is foo", "some category");
		BAR = soapAttachment("bar", "this is bar", "some other category");
		BAZ = soapAttachment("baz", "this is baz", "some category");
	}

	@Test
	public void allAttachmentsSelected() throws Exception {
		// given
		doReturn(asList(FOO, BAR, BAZ)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final CardDescriptor source = api().existingCard(CLASS_NAME, CARD_ID);

		// when
		selectedAttachments = api().existingCard(source).attachments().selectAll();

		// then
		final Iterable<AttachmentDescriptor> selected = selectedAttachments.selected();
		assertThat(size(selected), equalTo(3));
		assertThat(get(selected, 0).getName(), equalTo(FOO.getFilename()));
		assertThat(get(selected, 1).getName(), equalTo(BAR.getFilename()));

		verify(proxy()).getAttachmentList(eq(source.getClassName()), eq(source.getId()));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void someAttachmentsSelected() throws Exception {
		// given
		doReturn(asList(FOO, BAR, BAZ)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final CardDescriptor source = api().existingCard(CLASS_NAME, CARD_ID);

		// when
		selectedAttachments = api().existingCard(source) //
				.attachments() //
				.selectByName(FOO.getFilename(), BAZ.getFilename());

		// then
		final Iterable<AttachmentDescriptor> selected = selectedAttachments.selected();
		assertThat(size(selected), equalTo(2));
		assertThat(get(selected, 0).getName(), equalTo(FOO.getFilename()));
		assertThat(get(selected, 1).getName(), equalTo(BAZ.getFilename()));

		verify(proxy()).getAttachmentList(eq(source.getClassName()), eq(source.getId()));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void attachmentsDownloaded() throws Exception {
		// given
		doReturn(asList(FOO, BAR, BAZ)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final DataHandler dataHandler = new DataHandler(new FileDataSource(temporaryFolder.newFile()));
		doReturn(dataHandler) //
				.when(proxy()).downloadAttachment(anyString(), anyInt(), anyString());
		final CardDescriptor source = api().existingCard(CLASS_NAME, CARD_ID);

		// when
		final Iterable<Attachment> downloaded = api().existingCard(source) //
				.attachments() //
				.selectByName(FOO.getFilename(), BAZ.getFilename()) //
				.download();

		// then
		assertThat(size(downloaded), equalTo(2));
		assertThat(get(downloaded, 0).getName(), equalTo(FOO.getFilename()));
		assertThat(get(downloaded, 1).getName(), equalTo(BAZ.getFilename()));

		verify(proxy()).getAttachmentList(eq(source.getClassName()), eq(source.getId()));
		final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
		verify(proxy(), times(2)).downloadAttachment(eq(source.getClassName()), eq(source.getId()),
				fileNameCaptor.capture());
		verifyNoMoreInteractions(proxy());

		final List<String> values = fileNameCaptor.getAllValues();
		assertThat(values.size(), equalTo(2));
		assertThat(values.get(0), equalTo(FOO.getFilename()));
		assertThat(values.get(1), equalTo(BAZ.getFilename()));
	}

	@Test
	public void attachmentsDeleted() throws Exception {
		// given
		doReturn(asList(FOO, BAR, BAZ)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final CardDescriptor source = api().existingCard(CLASS_NAME, CARD_ID);

		// when
		api().existingCard(source) //
				.attachments() //
				.selectByName(FOO.getFilename(), BAZ.getFilename()) //
				.delete();

		// then
		verify(proxy()).getAttachmentList(eq(source.getClassName()), eq(source.getId()));
		final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
		verify(proxy(), times(2)).deleteAttachment(eq(source.getClassName()), eq(source.getId()),
				fileNameCaptor.capture());
		verifyNoMoreInteractions(proxy());

		final List<String> values = fileNameCaptor.getAllValues();
		assertThat(values.size(), equalTo(2));
		assertThat(values.get(0), equalTo(FOO.getFilename()));
		assertThat(values.get(1), equalTo(BAZ.getFilename()));
	}

	@Test
	public void attachmentsCopied() throws Exception {
		// given
		doReturn(asList(FOO, BAR, BAZ)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final CardDescriptor source = api().existingCard(CLASS_NAME, CARD_ID);
		final CardDescriptor destination = api().existingCard(CLASS_NAME + "2", CARD_ID * 10);

		// when
		api().existingCard(source) //
				.attachments() //
				.selectByName(FOO.getFilename(), BAZ.getFilename()) //
				.copyTo(destination);

		// then
		final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
		verify(proxy()).getAttachmentList(eq(source.getClassName()), eq(source.getId()));
		verify(proxy(), times(2)).copyAttachment(eq(source.getClassName()), eq(source.getId()),
				fileNameCaptor.capture(), eq(destination.getClassName()), eq(destination.getId()));
		verifyNoMoreInteractions(proxy());

		final List<String> values = fileNameCaptor.getAllValues();
		assertThat(values.size(), equalTo(2));
		assertThat(values.get(0), equalTo(FOO.getFilename()));
		assertThat(values.get(1), equalTo(BAZ.getFilename()));
	}

	@Test
	public void attachmentsMoved() throws Exception {
		// given
		doReturn(asList(FOO, BAR, BAZ)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());
		final CardDescriptor source = api().existingCard(CLASS_NAME, CARD_ID);
		final CardDescriptor destination = api().existingCard(CLASS_NAME + "2", CARD_ID * 10);

		// when
		api().existingCard(source) //
				.attachments() //
				.selectByName(FOO.getFilename(), BAZ.getFilename()) //
				.moveTo(destination);

		// then
		final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
		verify(proxy()).getAttachmentList(eq(source.getClassName()), eq(source.getId()));
		verify(proxy(), times(2)).moveAttachment(eq(source.getClassName()), eq(source.getId()),
				fileNameCaptor.capture(), eq(destination.getClassName()), eq(destination.getId()));
		verifyNoMoreInteractions(proxy());

		final List<String> values = fileNameCaptor.getAllValues();
		assertThat(values.size(), equalTo(2));
		assertThat(values.get(0), equalTo(FOO.getFilename()));
		assertThat(values.get(1), equalTo(BAZ.getFilename()));
	}

	private static org.cmdbuild.services.soap.Attachment soapAttachment(final String _filename,
			final String _description, final String _category) {
		final org.cmdbuild.services.soap.Attachment foo = new org.cmdbuild.services.soap.Attachment() {
			{
				setFilename(_filename);
				setDescription(_description);
				setCategory(_category);
			}
		};
		return foo;
	}

}
