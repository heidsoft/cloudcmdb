package unit.api.fluent.ws;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.activation.DataHandler;

import org.cmdbuild.api.fluent.Attachment;
import org.cmdbuild.api.fluent.AttachmentDescriptor;
import org.cmdbuild.api.fluent.Attachments;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

public class AttachmentsTest extends AbstractWsFluentApiTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private Attachments attachments;

	@Before
	public void createAttachments() throws Exception {
		final CardDescriptor source = api().existingCard(CLASS_NAME, CARD_ID);
		attachments = api().existingCard(source).attachments();
	}

	@Test
	public void fetchAttachments() throws Exception {
		// given
		final org.cmdbuild.services.soap.Attachment foo = new org.cmdbuild.services.soap.Attachment() {
			{
				setFilename("foo");
				setDescription("this is foo");
				setCategory("some category");
			}
		};
		final org.cmdbuild.services.soap.Attachment bar = new org.cmdbuild.services.soap.Attachment() {
			{
				setFilename("bar");
				setDescription("this is bar");
				setCategory("some other category");
			}
		};
		doReturn(asList(foo, bar)) //
				.when(proxy()).getAttachmentList(anyString(), anyInt());

		// when
		final Iterable<AttachmentDescriptor> descriptors = attachments.fetch();

		// then
		assertThat(size(descriptors), equalTo(2));
		final AttachmentDescriptor first = get(descriptors, 0);
		assertThat(first.getName(), equalTo("foo"));
		assertThat(first.getDescription(), equalTo("this is foo"));
		assertThat(first.getCategory(), equalTo("some category"));
		final AttachmentDescriptor second = get(descriptors, 1);
		assertThat(second.getName(), equalTo("bar"));
		assertThat(second.getDescription(), equalTo("this is bar"));
		assertThat(second.getCategory(), equalTo("some other category"));

		verify(proxy()).getAttachmentList(eq(CLASS_NAME), eq(CARD_ID));
		verifyNoMoreInteractions(proxy());
	}

	@Test
	public void uploadMultipleAttachments() throws Exception {
		// given
		final String firstUrl = temporaryFolder.newFile().toURI().toURL().toString();
		final String secondUrl = temporaryFolder.newFile().toURI().toURL().toString();

		final Attachment foo = mock(Attachment.class);
		doReturn("foo").when(foo).getName();
		doReturn("this is foo").when(foo).getDescription();
		doReturn("some category").when(foo).getCategory();
		doReturn(firstUrl).when(foo).getUrl();

		final Attachment bar = mock(Attachment.class);
		doReturn("bar").when(bar).getName();
		doReturn("this is bar").when(bar).getDescription();
		doReturn("some other category").when(bar).getCategory();
		doReturn(secondUrl).when(bar).getUrl();

		// when
		attachments.upload(foo, bar);

		// then
		final ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> categoryCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
		verify(proxy(), times(2)).uploadAttachment(eq(CLASS_NAME), eq(CARD_ID), any(DataHandler.class),
				fileNameCaptor.capture(), categoryCaptor.capture(), descriptionCaptor.capture());
		verifyNoMoreInteractions(proxy());

		assertThat(fileNameCaptor.getAllValues().get(0), equalTo("foo"));
		assertThat(descriptionCaptor.getAllValues().get(0), equalTo("this is foo"));
		assertThat(categoryCaptor.getAllValues().get(0), equalTo("some category"));
		assertThat(fileNameCaptor.getAllValues().get(1), equalTo("bar"));
		assertThat(descriptionCaptor.getAllValues().get(1), equalTo("this is bar"));
		assertThat(categoryCaptor.getAllValues().get(1), equalTo("some other category"));
	}

	@Test
	public void uploadSingleAttachment() throws Exception {
		// given
		final String url = temporaryFolder.newFile().toURI().toURL().toString();

		// when
		attachments.upload("foo", "this is foo", "some category", url);

		// then
		verify(proxy()).uploadAttachment(eq(CLASS_NAME), eq(CARD_ID), any(DataHandler.class), eq("foo"),
				eq("some category"), eq("this is foo"));
		verifyNoMoreInteractions(proxy());
	}

}
