package org.cmdbuild.common.api.mail.javax.mail;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static javax.mail.Part.ATTACHMENT;
import static javax.mail.Part.INLINE;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.Header.CC;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.Header.RECIPIENTS_SEPARATOR;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.Header.TO;
import static org.cmdbuild.common.api.mail.javax.mail.Utils.NO_HEADER_FOUND;
import static org.cmdbuild.common.api.mail.javax.mail.Utils.headersOf;
import static org.cmdbuild.common.api.mail.javax.mail.Utils.messageIdOf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.FileUtils;
import org.cmdbuild.common.api.mail.Configuration.Input;
import org.cmdbuild.common.api.mail.FetchedMail;
import org.cmdbuild.common.api.mail.GetMail;
import org.cmdbuild.common.api.mail.GetMail.Attachment;
import org.cmdbuild.common.api.mail.MailException;
import org.cmdbuild.common.api.mail.SelectMail;
import org.cmdbuild.common.api.mail.javax.mail.InputTemplate.Hook;
import org.slf4j.Logger;

import com.google.common.base.Function;

class SelectMailImpl implements SelectMail {

	private class DefaultAttachment implements Attachment {

		private final String filename;
		private final DataHandler dataHandler;

		public DefaultAttachment(final String filename, final File file) {
			this.filename = filename;
			this.dataHandler = new DataHandler(new FileDataSource(file));
		}

		@Override
		public String getName() {
			return filename;
		}

		@Override
		public DataHandler getDataHandler() {
			return dataHandler;
		}

	}

	private class ContentExtractor {

		private static final String TEXT_PLAIN = "text/plain";

		private static final String ATTACHMENT_PREFIX = "attachment";
		private static final String ATTACHMENT_EXTENSION = ".out";

		private final Logger logger = SelectMailImpl.this.logger;

		private String content = EMPTY;
		private String plainAlternative = EMPTY;
		private final Collection<Attachment> attachments = newArrayList();

		public ContentExtractor(final Message message) throws MailException {
			try {
				handle(message);
				content = defaultString(defaultString(content, plainAlternative), "Mail content not recognized");
			} catch (final MessagingException e) {
				throw MailException.content(e);
			} catch (final IOException e) {
				throw MailException.io(e);
			}
		}

		private void handle(final Message message) throws MessagingException, IOException {
			final Object content = message.getContent();
			if (content instanceof Multipart) {
				handleMultipart(Multipart.class.cast(content));
			} else {
				handlePart(message);
			}
		}

		private void handleMultipart(final Multipart multipart) throws MessagingException, IOException {
			for (int i = 0, n = multipart.getCount(); i < n; i++) {
				handlePart(multipart.getBodyPart(i));
			}
		}

		private void handlePart(final Part part) throws MessagingException, IOException {
			final String contentType = part.getContentType();
			logger.debug("content-type for current part is '{}'", contentType);
			final String disposition = part.getDisposition();
			if (disposition == null) {
				final String tempContent = parseContent(part.getContent());
				if (!tempContent.isEmpty()) {
					content = tempContent;
				}
			} else if (ATTACHMENT.equalsIgnoreCase(disposition)) {
				logger.debug("attachment with name '{}'", part.getFileName());
				handleAttachment(part);
			} else if (INLINE.equalsIgnoreCase(disposition)) {
				logger.debug("in-line with name '{}'", part.getFileName());
				if (part.getFileName() != null) {
					handleAttachment(part);
				} else {
					content = parseContent(part.getContent());
				}
			} else {
				logger.warn("should never happen, disposition is '{}'", disposition);
			}
			if (contentType.toLowerCase().contains(TEXT_PLAIN)) {
				if (plainAlternative.isEmpty()) {
					plainAlternative = String.class.cast(part.getContent());
				}
			}
		}

		private String parseContent(final Object messageContent) throws IOException, MessagingException {
			if (messageContent == null) {
				throw new IllegalArgumentException();
			}
			String parsedMessage = EMPTY;
			if (messageContent instanceof Multipart) {
				final Multipart mp = (Multipart) messageContent;
				for (int i = 0, n = mp.getCount(); i < n; ++i) {
					final Part part = mp.getBodyPart(i);
					final String ctype = part.getContentType();
					final String disposition = part.getDisposition();
					if (disposition == null) {
						final Object content = part.getContent();
						if (ctype.toLowerCase().contains(TEXT_PLAIN)) {
							if (plainAlternative.isEmpty()) {
								plainAlternative = String.class.cast(content);
							}
						} else {
							if (content instanceof String) {
								parsedMessage += content;
							} else if (content instanceof Multipart) {
								parsedMessage += parseContent(content);
							}
						}
					}
				}
			} else if (messageContent instanceof String) {
				parsedMessage = messageContent.toString();
			}
			return parsedMessage;
		}

		private void handleAttachment(final Part part) throws MessagingException, IOException {
			final File directory = FileUtils.getTempDirectory();
			final File file;
			final String filename;
			if (part.getFileName() == null) {
				file = File.createTempFile(ATTACHMENT_PREFIX, ATTACHMENT_EXTENSION, directory);
				file.deleteOnExit();
				filename = file.getName();
			} else {
				filename = MimeUtility.decodeText(part.getFileName());
				file = File.createTempFile(filename, null, directory);
			}
			file.deleteOnExit();
			logger.trace("saving file '{}'", file.getPath());

			final InputStream is = part.getInputStream();
			FileUtils.copyInputStreamToFile(is, file);

			attachments.add(new DefaultAttachment(filename, file));
		}

		public String getContent() {
			return content;
		}

		public Iterable<Attachment> getAttachments() {
			return attachments;
		}

	}

	private static final String ADDRESS_PATTERN_REGEX = ".*<(.*)>.*";
	private static final Pattern ADDRESS_PATTERN = Pattern.compile(ADDRESS_PATTERN_REGEX);

	private final Input configuration;
	private final Logger logger;
	private final FetchedMail mail;

	private GetMail getMail;
	private String targetFolder;

	public SelectMailImpl(final Input configuration, final FetchedMail fetchedMail) {
		this.configuration = configuration;
		this.logger = configuration.getLogger();
		this.mail = fetchedMail;
	}

	@Override
	public GetMail get() throws MailException {
		logger.info("getting specified mail");
		final InputTemplate inputTemplate = new InputTemplate(configuration);
		inputTemplate.execute(new Hook() {

			@Override
			public void connected(final Store store) throws MailException {
				try {
					final Folder inbox = store.getFolder(mail.getFolder());
					inbox.open(Folder.READ_ONLY);
					for (final Message message : inbox.getMessages()) {
						if (mail.getId().equals(messageIdOf(message))) {
							getMail = transform(message);
							break;
						}
					}
				} catch (final MessagingException e) {
					logger.error("error getting mail", e);
					throw MailException.get(e);
				}
			}

		});
		return getMail;
	}

	private GetMail transform(final Message message) throws MessagingException {
		final ContentExtractor contentExtractor = new ContentExtractor(message);
		return GetMailImpl.newInstance() //
				.withId(messageIdOf(message)) //
				.withFolder(message.getFolder().getFullName()) //
				.withSubject(message.getSubject()) //
				.withFrom(stripAddress(firstOf(message.getFrom()))) //
				.withTos(splitRecipients(headersOf(message, TO))) //
				.withCcs(splitRecipients(headersOf(message, CC))) //
				.withContent(contentExtractor.getContent()) //
				.withAttachments(contentExtractor.getAttachments()) //
				.build();
	}

	private String firstOf(final Address[] froms) {
		return isEmpty(froms) ? null : froms[0].toString();
	}

	private Iterable<String> splitRecipients(final String[] headers) {
		if ((headers != NO_HEADER_FOUND) && (headers.length > 0)) {
			final String recipients = headers[0];
			return from(asList(recipients.split(RECIPIENTS_SEPARATOR))) //
					.transform(new Function<String, String>() {

						@Override
						public String apply(final String input) {
							return trim(stripAddress(input));
						}

					});
		}
		return emptyList();
	}

	private String stripAddress(final String input) {
		final Matcher matcher = ADDRESS_PATTERN.matcher(input);
		return matcher.matches() ? matcher.group(1) : input;
	}

	@Override
	public SelectMail selectTargetFolder(final String folder) {
		logger.info("selects folder '{}'", folder);
		this.targetFolder = folder;
		return this;
	}

	@Override
	public void move() throws MailException {
		logger.info("moving mail with id '{}' to folder '{}'", mail.getId(), targetFolder);
		final InputTemplate inputTemplate = new InputTemplate(configuration);
		inputTemplate.execute(new Hook() {

			@Override
			public void connected(final Store store) throws MailException {
				try {
					final Folder inbox = store.getFolder(mail.getFolder());
					inbox.open(Folder.READ_WRITE);
					for (final Message message : inbox.getMessages()) {
						if (mail.getId().equals(messageIdOf(message))) {
							final Message[] singleMessageArray = new Message[] { message };
							inbox.copyMessages(singleMessageArray, getOrCreate(store, targetFolder));
							inbox.setFlags(singleMessageArray, new Flags(Flags.Flag.DELETED), true);
							inbox.expunge();
							break;
						}
					}
				} catch (final MessagingException e) {
					logger.error("error moving mail", e);
					throw MailException.move(e);
				}
			}

			private Folder getOrCreate(final Store store, final String name) throws MessagingException {
				final Folder folder = store.getFolder(name);
				if (!folder.exists()) {
					folder.create(Folder.HOLDS_MESSAGES);
				}
				return folder;
			}

		});
	}

}
