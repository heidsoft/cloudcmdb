package unit.cxf;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.model.Models.newAttachment;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.cmdbuild.service.rest.v1.cxf.AttachmentsHelper;
import org.cmdbuild.service.rest.v1.cxf.TranslatingAttachmentsHelper;
import org.cmdbuild.service.rest.v1.cxf.TranslatingAttachmentsHelper.Encoding;
import org.cmdbuild.service.rest.v1.model.Attachment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Optional;

public class TranslatingAttachmentsHelperTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private AttachmentsHelper delegate;
	private Encoding encoding;
	private TranslatingAttachmentsHelper translatingAttachmentsHelper;

	@Before
	public void setUp() throws Exception {
		delegate = mock(AttachmentsHelper.class);
		encoding = mock(Encoding.class);
		translatingAttachmentsHelper = new TranslatingAttachmentsHelper(delegate, encoding);

		doReturn("encoded value") //
				.when(encoding).encode(anyString());
		doReturn("decoded value") //
				.when(encoding).decode(anyString());
	}

	@Test
	public void create() throws Exception {
		final Attachment attachment = newAttachment() //
				.withName("the name") //
				.withDescription("the description") //
				.withCategory("the category") //
				.build();
		final DataHandler dataHandler = new DataHandler(new FileDataSource(temporaryFolder.newFile()));
		doReturn("the id") //
				.when(delegate).create(anyString(), anyLong(), anyString(), any(Attachment.class),
						any(DataHandler.class));

		// when
		final String id = translatingAttachmentsHelper.create("foo", 123L, "bar", attachment, dataHandler);

		// then
		assertThat(id, equalTo("encoded value"));
		verify(delegate).create(eq("foo"), eq(123L), eq("bar"), eq(attachment), eq(dataHandler));
		verify(encoding).encode(eq("the id"));
		verifyNoMoreInteractions(encoding, delegate);
	}

	@Test
	public void update() throws Exception {
		final Attachment attachment = newAttachment() //
				.withName("the name") //
				.withDescription("the description") //
				.withCategory("the category") //
				.build();
		final DataHandler dataHandler = new DataHandler(new FileDataSource(temporaryFolder.newFile()));

		// when
		translatingAttachmentsHelper.update("foo", 123L, "bar", attachment, dataHandler);

		// then
		verify(encoding).decode(eq("bar"));
		verify(delegate).update(eq("foo"), eq(123L), eq("decoded value"), eq(attachment), eq(dataHandler));
		verifyNoMoreInteractions(encoding, delegate);
	}

	@Test
	public void searchAll() throws Exception {
		// given
		final Attachment attachment = newAttachment() //
				.withId("the id") //
				.withName("the name") //
				.withDescription("the description") //
				.withCategory("the category") //
				.build();
		doReturn(asList(attachment)) //
				.when(delegate).search(anyString(), anyLong());

		// when
		final Iterable<Attachment> found = translatingAttachmentsHelper.search("foo", 123L);

		// then
		assertThat(newArrayList(found), equalTo(asList(newAttachment(attachment) //
				.withId("encoded value") //
				.build())));
		verify(delegate).search(eq("foo"), eq(123L));
		verify(encoding).encode(eq("the id"));
		verifyNoMoreInteractions(encoding, delegate);
	}

	@Test
	public void searchSingle() throws Exception {
		// given
		final Attachment attachment = newAttachment() //
				.withId("the id") //
				.withName("the name") //
				.withDescription("the description") //
				.withCategory("the category") //
				.build();
		doReturn(Optional.of(attachment)) //
				.when(delegate).search(anyString(), anyLong(), anyString());

		// when
		final Optional<Attachment> found = translatingAttachmentsHelper.search("foo", 123L, "bar");

		// then
		assertThat(found.get(), equalTo(newAttachment(attachment) //
				.withId("encoded value") //
				.build()));
		verify(encoding).decode(eq("bar"));
		verify(delegate).search(eq("foo"), eq(123L), eq("decoded value"));
		verify(encoding).encode(eq("the id"));
		verifyNoMoreInteractions(encoding, delegate);
	}

	@Test
	public void download() throws Exception {
		// when
		translatingAttachmentsHelper.download("foo", 123L, "bar");

		// then
		verify(encoding).decode(eq("bar"));
		verify(delegate).download(eq("foo"), eq(123L), eq("decoded value"));
		verifyNoMoreInteractions(encoding, delegate);
	}

	@Test
	public void delete() throws Exception {
		// when
		translatingAttachmentsHelper.delete("foo", 123L, "bar");

		// then
		verify(encoding).decode(eq("bar"));
		verify(delegate).delete(eq("foo"), eq(123L), eq("decoded value"));
		verifyNoMoreInteractions(encoding, delegate);
	}

}
