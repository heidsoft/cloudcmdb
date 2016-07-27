package org.cmdbuild.common.api.mail.javax.mail;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.net.URL;

import javax.activation.DataHandler;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

import org.cmdbuild.common.api.mail.Configuration.Input;
import org.cmdbuild.common.api.mail.Configuration.Output;
import org.cmdbuild.common.api.mail.ForwardingNewMail;
import org.cmdbuild.common.api.mail.NewMail;
import org.cmdbuild.common.api.mail.SendableNewMail;
import org.cmdbuild.common.api.mail.javax.mail.OutputTemplate.Hook;
import org.slf4j.Logger;

class SendableNewMailImpl extends ForwardingNewMail implements SendableNewMail {

	private final Input input;
	private final Output output;
	private final Logger logger;

	private final NewMailImpl newMail;
	private boolean asynchronous;

	public SendableNewMailImpl(final Input input, final Output output) {
		this.input = input;
		this.output = output;
		this.logger = output.getLogger();
		newMail = new NewMailImpl(logger);
	}

	@Override
	protected NewMail delegate() {
		return newMail;
	}

	@Override
	public SendableNewMail withFrom(final String from) {
		delegate().withFrom(from);
		return this;
	}

	@Override
	public SendableNewMail withTo(final String to) {
		delegate().withTo(to);
		return this;
	}

	@Override
	public SendableNewMail withTo(final String... tos) {
		delegate().withTo(tos);
		return this;
	}

	@Override
	public SendableNewMail withTo(final Iterable<String> tos) {
		delegate().withTo(tos);
		return this;
	}

	@Override
	public SendableNewMail withCc(final String cc) {
		delegate().withCc(cc);
		return this;
	}

	@Override
	public SendableNewMail withCc(final String... ccs) {
		delegate().withCc(ccs);
		return this;
	}

	@Override
	public SendableNewMail withCc(final Iterable<String> ccs) {
		delegate().withCc(ccs);
		return this;
	}

	@Override
	public SendableNewMail withBcc(final String bcc) {
		delegate().withBcc(bcc);
		return this;
	}

	@Override
	public SendableNewMail withBcc(final String... bccs) {
		delegate().withBcc(bccs);
		return this;
	}

	@Override
	public SendableNewMail withBcc(final Iterable<String> bccs) {
		delegate().withBcc(bccs);
		return this;
	}

	@Override
	public SendableNewMail withSubject(final String subject) {
		delegate().withSubject(subject);
		return this;
	}

	@Override
	public SendableNewMail withContent(final String body) {
		delegate().withContent(body);
		return this;
	}

	@Override
	public SendableNewMail withContentType(final String contentType) {
		delegate().withContentType(contentType);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final URL url) {
		delegate().withAttachment(url);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final URL url, final String name) {
		delegate().withAttachment(url, name);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final String url) {
		delegate().withAttachment(url);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final String url, final String name) {
		delegate().withAttachment(url, name);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final DataHandler dataHandler) {
		delegate().withAttachment(dataHandler);
		return this;
	}

	@Override
	public SendableNewMail withAttachment(final DataHandler dataHandler, final String name) {
		delegate().withAttachment(dataHandler, name);
		return this;
	}

	@Override
	public SendableNewMail withAsynchronousSend(final boolean asynchronous) {
		this.asynchronous = asynchronous;
		return this;
	}

	@Override
	public void send() {
		final Runnable job = sendJob();
		if (asynchronous) {
			runInAnotherThread(job);
		} else {
			job.run();
		}
	}

	private Runnable sendJob() {
		return new Runnable() {

			@Override
			public void run() {
				try {
					send0();
				} catch (final RuntimeException e) {
					logger.error("error sending e-mail", e);
					throw e;
				}
			}

		};
	}

	private void send0() {
		new OutputTemplate(output).execute(new Hook() {

			@Override
			public void connected(final Session session, final Transport transport) throws MessagingException {
				logger.debug("sending e-mail '{}'", newMail);
				final MessageBuilder messageBuilder = new NewMailImplMessageBuilder(output, session, newMail);
				final Message message = messageBuilder.build();
				transport.sendMessage(message, message.getAllRecipients());
				tryStoreMessage(message);
			}

			private void tryStoreMessage(final Message message) {
				try {
					if (isNotBlank(output.getOutputFolder())) {
						new InputTemplate(input).execute(new InputTemplate.Hook() {

							@Override
							public void connected(final Store store) throws MessagingException {
								logger.debug("storing e-mail '{}'", newMail);
								final Folder folder = store.getFolder(output.getOutputFolder());
								if (!folder.exists()) {
									folder.create(Folder.HOLDS_MESSAGES);
								}
								folder.open(Folder.READ_WRITE);
								folder.appendMessages(new Message[] { message });
								message.setFlag(Flags.Flag.RECENT, true);
							}

						});
					}
				} catch (final Exception e) {
					logger.error("error storing e-mail", e);
				}
			}

		});
	}

	private void runInAnotherThread(final Runnable job) {
		new Thread(job).start();
	}

}
