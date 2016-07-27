package org.cmdbuild.common.api.mail.javax.mail;

import static org.cmdbuild.common.api.mail.javax.mail.Utils.messageIdOf;

import java.util.Arrays;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import org.cmdbuild.common.api.mail.Configuration.Input;
import org.cmdbuild.common.api.mail.FetchedMail;
import org.cmdbuild.common.api.mail.MailException;
import org.cmdbuild.common.api.mail.SelectFolder;
import org.cmdbuild.common.api.mail.javax.mail.InputTemplate.Hook;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

class SelectFolderImpl implements SelectFolder {

	private final Input configuration;
	private final String folderName;
	private final Logger logger;

	public SelectFolderImpl(final Input configuration, final String folder) {
		this.configuration = configuration;
		this.folderName = folder;
		this.logger = configuration.getLogger();
	}

	@Override
	public List<FetchedMail> fetch() throws MailException {
		logger.info("fetching folder '{}' for mails", folderName);
		final List<FetchedMail> fetchedMails = Lists.newArrayList();
		final InputTemplate inputTemplate = new InputTemplate(configuration);
		inputTemplate.execute(new Hook() {

			@Override
			public void connected(final Store store) throws MailException {
				try {
					final Folder folder = store.getFolder(folderName);
					folder.open(Folder.READ_ONLY);

					final List<Message> messages = Arrays.asList(folder.getMessages());
					for (final Message message : messages) {
						fetchedMails.add(transform(message));
					}
				} catch (final MessagingException e) {
					logger.error("error fetching mails", e);
					throw MailException.fetch(e);
				}
			}

		});
		return fetchedMails;
	}

	private FetchedMail transform(final Message message) throws MessagingException {
		return FetchedMailImpl.newInstance() //
				.withId(messageIdOf(message)) //
				.withFolder(message.getFolder().getFullName()) //
				.withSubject(message.getSubject()) //
				.build();
	}

}
