package integration.api.mail.javax.mail;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.cmdbuild.common.api.mail.NewMailQueue;
import org.cmdbuild.common.api.mail.NewMailQueue.Callback;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

public class SendMailWithQueueTest extends AbstractMailTest {

	private static final Comparator<MimeMessage> MESSAGES_BY_SUBJECT = new Comparator<MimeMessage>() {

		@Override
		public int compare(final MimeMessage o1, final MimeMessage o2) {
			try {
				return o1.getSubject().compareTo(o2.getSubject());
			} catch (final MessagingException e) {
				return 0;
			}
		}

	};

	protected static final String ATTACHMENT_CONTENT = UUID.randomUUID().toString();
	protected static final int ATTACHMENT_BODY_PART = 1;

	@Rule
	public GreenMailServer greenMailServer = GreenMailServer.newInstance() //
			.withConfiguration(outputServerSetup()) //
			.build();

	@Override
	protected ServerSetup inputServerSetup() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ServerSetup outputServerSetup() {
		return ServerSetupTest.SMTP;
	}

	@Override
	protected GreenMail greenMail() {
		return greenMailServer.getServer();
	}

	@Test
	public void ifNotSpecifiedFromsAreTheValuesSpecifiedWithinTheConfiguration() throws Exception {
		newMailQueue() //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getFrom().length, equalTo(1));
		assertThat(receivedMessage.getFrom()[0].toString(), equalTo(FOO_AT_EXAMPLE_DOT_COM));
	}

	@Test
	public void ifFromIsSpecifiedFromIsNotReadedFromConfiguration() throws Exception {
		newMailQueue() //
				.newMail() //
				.withFrom(BAR_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAZ_AT_EXAMPLE_DOT_COM) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getFrom().length, equalTo(2));
		assertThat(receivedMessage.getFrom()[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getFrom()[1].toString(), equalTo(BAZ_AT_EXAMPLE_DOT_COM));
	}

	@Test
	public void multipleFromsWithTheSameValueAreAcceptedButOnlyOneIsUsed() throws Exception {
		newMailQueue() //
				.newMail() //
				.withFrom(BAZ_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAR_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAZ_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAR_AT_EXAMPLE_DOT_COM) //
				.withFrom(BAZ_AT_EXAMPLE_DOT_COM) //
				.withTo(FOO_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getFrom().length, equalTo(2));
		assertThat(receivedMessage.getFrom()[0].toString(), equalTo(BAZ_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getFrom()[1].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
	}

	@Test
	public void onlyToRecipientsOthersAreNull() throws Exception {
		newMailQueue() //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO), not(is(nullValue())));
		assertThat(receivedMessage.getRecipients(RecipientType.CC), is(nullValue()));
		assertThat(receivedMessage.getRecipients(RecipientType.BCC), is(nullValue()));
	}

	@Test
	public void onlyCcRecipientsOthersAreNull() throws Exception {
		newMailQueue() //
				.newMail() //
				.withCc(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO), is(nullValue()));
		assertThat(receivedMessage.getRecipients(RecipientType.CC), not(is(nullValue())));
		assertThat(receivedMessage.getRecipients(RecipientType.BCC), is(nullValue()));
	}

	@Test
	public void onlyBccRecipientsOthersAreNull() throws Exception {
		newMailQueue() //
				.newMail() //
				.withBcc(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO), is(nullValue()));
		assertThat(receivedMessage.getRecipients(RecipientType.CC), is(nullValue()));
		assertThat(receivedMessage.getRecipients(RecipientType.BCC), is(nullValue()));
	}

	@Test
	public void messageWithoutContentSuccessfullySent() throws Exception {
		newMailQueue(FOO, PASSWORD) //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO)[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
	}

	@Test
	public void plainTextMessageSuccessfullySent() throws Exception {
		newMailQueue() //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO)[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
		assertThat(getBody(receivedMessage), equalTo(PLAIN_TEXT_CONTENT));
	}

	@Test
	public void plainTextMessageSuccessfullySentWithAutentication() throws Exception {
		greenMail().setUser(FOO_AT_EXAMPLE_DOT_COM, FOO, PASSWORD);

		newMailQueue(FOO, PASSWORD) //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		assertThat(receivedMessage.getRecipients(RecipientType.TO)[0].toString(), equalTo(BAR_AT_EXAMPLE_DOT_COM));
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
		assertThat(getBody(receivedMessage), equalTo(PLAIN_TEXT_CONTENT));
	}

	@Test
	public void attachmentSuccessfullySent() throws Exception {
		final URL attachment = newAttachmentFileFromContent(ATTACHMENT_CONTENT);

		newMailQueue() //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.withAttachment(attachment) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		final MimeMultipart mimeMultipart = (MimeMultipart) receivedMessage.getContent();
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
		assertThat(getBody(mimeMultipart.getBodyPart(0)), equalTo(PLAIN_TEXT_CONTENT));
		assertThat(receivedAttachmentContent(), equalTo(ATTACHMENT_CONTENT));
	}

	@Test
	public void attachmentByStringSuccessfullySent() throws Exception {
		final URL attachment = newAttachmentFileFromContent(ATTACHMENT_CONTENT);

		newMailQueue() //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.withAttachment(attachment.toString()) //
				.add() //
				.sendAll();

		final MimeMessage receivedMessage = firstReceivedMessage();
		final MimeMultipart mimeMultipart = (MimeMultipart) receivedMessage.getContent();
		assertThat(receivedMessage.getSubject(), equalTo(SUBJECT));
		assertThat(getBody(mimeMultipart.getBodyPart(0)), equalTo(PLAIN_TEXT_CONTENT));
		assertThat(receivedAttachmentContent(), equalTo(ATTACHMENT_CONTENT));
	}

	@Test
	public void defaultMimeTypeIsTextPlain() throws Exception {
		newMailQueue() //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				.sendAll();

		assertThat(firstReceivedMessage().getContentType(), startsWith(MIME_TEXT_PLAIN));
	}

	@Test
	public void customMimeTypeSuccessfullySetted() throws Exception {
		newMailQueue() //
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(SUBJECT) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.withContentType(MIME_TEXT_HTML) //
				.add() //
				.sendAll();

		assertThat(((MimeMultipart) firstReceivedMessage().getContent()).getBodyPart(0).getContentType(),
				startsWith(MIME_TEXT_HTML));
	}

	@Test
	public void multipleMessagesSuccessfullySent() throws Exception {
		newMailQueue() //
				// ---
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(FOO) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				// ---
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(BAR) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				// ---
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(BAZ) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				// ---
				.sendAll();

		final List<MimeMessage> receivedMessages = Arrays.asList(greenMail().getReceivedMessages());
		assertThat(receivedMessages, hasSize(3));
		Collections.sort(receivedMessages, MESSAGES_BY_SUBJECT);
		assertThat(receivedMessages.get(0).getSubject(), equalTo(BAR));
		assertThat(receivedMessages.get(1).getSubject(), equalTo(BAZ));
		assertThat(receivedMessages.get(2).getSubject(), equalTo(FOO));
	}

	@Test
	public void callbackCalledWhenMailsAreAddedAndSent() throws Exception {
		// given
		final Callback callback = mock(Callback.class);
		final NewMailQueue queue = newMailQueue() //
				.withCallback(callback);

		// when
		queue.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(FOO) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add();

		// then
		verify(callback).added(eq(0));

		// and when
		queue.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(BAR) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add();

		// then
		verify(callback).added(eq(1));

		// and when
		queue.sendAll();

		// then
		final ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
		verify(callback, times(2)).sent(captor.capture());
		assertThat(captor.getAllValues().get(0), equalTo(0));
		assertThat(captor.getAllValues().get(1), equalTo(1));

		final List<MimeMessage> receivedMessages = Arrays.asList(greenMail().getReceivedMessages());
		assertThat(receivedMessages, hasSize(2));
		Collections.sort(receivedMessages, MESSAGES_BY_SUBJECT);
		assertThat(receivedMessages.get(0).getSubject(), equalTo(BAR));
		assertThat(receivedMessages.get(1).getSubject(), equalTo(FOO));
	}

	@Test
	public void almostAllMessagesSuccessfullySent() throws Exception {
		newMailQueue() //
				//
				.withForgiving(true) //
				// ---
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(FOO) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				// ---
				.newMail() //
				.withSubject(BAR) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				// ---
				.newMail() //
				.withTo(BAR_AT_EXAMPLE_DOT_COM) //
				.withSubject(BAZ) //
				.withContent(PLAIN_TEXT_CONTENT) //
				.add() //
				// ---
				.sendAll();

		final List<MimeMessage> receivedMessages = Arrays.asList(greenMail().getReceivedMessages());
		assertThat(receivedMessages, hasSize(2));
		Collections.sort(receivedMessages, MESSAGES_BY_SUBJECT);
		assertThat(receivedMessages.get(0).getSubject(), equalTo(BAZ));
		assertThat(receivedMessages.get(1).getSubject(), equalTo(FOO));
	}

}
