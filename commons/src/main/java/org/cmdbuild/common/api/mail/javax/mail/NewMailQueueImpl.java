package org.cmdbuild.common.api.mail.javax.mail;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

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
import org.cmdbuild.common.api.mail.NewMailQueue;
import org.cmdbuild.common.api.mail.QueueableNewMail;
import org.slf4j.Logger;

class NewMailQueueImpl implements NewMailQueue {

	private static class QueueableNewMailImpl extends ForwardingNewMail implements QueueableNewMail {

		private final NewMailQueue parent;
		private final Callback callback;
		private final NewMailImpl newMail;
		private final Collection<? super NewMailImpl> elements;

		public QueueableNewMailImpl(final NewMailQueue parent, final Callback callback, final NewMailImpl newMail,
				final Collection<? super NewMailImpl> elements) {
			this.parent = parent;
			this.callback = callback;
			this.newMail = newMail;
			this.elements = elements;
		}

		@Override
		protected NewMail delegate() {
			return newMail;
		}

		@Override
		public QueueableNewMail withFrom(final String from) {
			delegate().withFrom(from);
			return this;
		}

		@Override
		public QueueableNewMail withTo(final String to) {
			delegate().withTo(to);
			return this;
		}

		@Override
		public QueueableNewMail withTo(final String... tos) {
			delegate().withTo(tos);
			return this;
		}

		@Override
		public QueueableNewMail withTo(final Iterable<String> tos) {
			delegate().withTo(tos);
			return this;
		}

		@Override
		public QueueableNewMail withCc(final String cc) {
			delegate().withCc(cc);
			return this;
		}

		@Override
		public QueueableNewMail withCc(final String... ccs) {
			delegate().withCc(ccs);
			return this;
		}

		@Override
		public QueueableNewMail withCc(final Iterable<String> ccs) {
			delegate().withCc(ccs);
			return this;
		}

		@Override
		public QueueableNewMail withBcc(final String bcc) {
			delegate().withBcc(bcc);
			return this;
		}

		@Override
		public QueueableNewMail withBcc(final String... bccs) {
			delegate().withBcc(bccs);
			return this;
		}

		@Override
		public QueueableNewMail withBcc(final Iterable<String> bccs) {
			delegate().withBcc(bccs);
			return this;
		}

		@Override
		public QueueableNewMail withSubject(final String subject) {
			delegate().withSubject(subject);
			return this;
		}

		@Override
		public QueueableNewMail withContent(final String body) {
			delegate().withContent(body);
			return this;
		}

		@Override
		public QueueableNewMail withContentType(final String contentType) {
			delegate().withContentType(contentType);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final URL url) {
			delegate().withAttachment(url);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final URL url, final String name) {
			delegate().withAttachment(url, name);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final String url) {
			delegate().withAttachment(url);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final String url, final String name) {
			delegate().withAttachment(url, name);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final DataHandler dataHandler) {
			delegate().withAttachment(dataHandler);
			return this;
		}

		@Override
		public QueueableNewMail withAttachment(final DataHandler dataHandler, final String name) {
			delegate().withAttachment(dataHandler, name);
			return this;
		}

		@Override
		public NewMailQueue add() {
			elements.add(newMail);
			callback.added(elements.size() - 1);
			return parent;
		}

	}

	private static final Callback NULL_CALLBACK = new Callback() {

		@Override
		public void added(final int index) {
			// nothing to do
		}

		@Override
		public void sent(final int index) {
			// nothing to do
		}

	};

	private final Input input;
	private final Output output;
	private final Logger logger;
	private final Collection<NewMailImpl> elements;
	private Callback callback = NULL_CALLBACK;
	private boolean forgiving;

	public NewMailQueueImpl(final Input input, final Output output) {
		this.input = input;
		this.output = output;
		this.logger = output.getLogger();
		// we need to preserve order
		this.elements = newArrayList();
	}

	@Override
	public NewMailQueue withCallback(final Callback callback) {
		this.callback = defaultIfNull(callback, NULL_CALLBACK);
		return this;
	}

	@Override
	public NewMailQueue withForgiving(final boolean forgiving) {
		this.forgiving = forgiving;
		return this;
	}

	@Override
	public QueueableNewMail newMail() {
		final NewMailImpl newMail = new NewMailImpl(logger);
		return new QueueableNewMailImpl(this, callback, newMail, elements);
	}

	@Override
	public void sendAll() {
		final Map<Message, NewMail> sent = send();
		store(sent);
	}

	private Map<Message, NewMail> send() {
		final Map<Message, NewMail> sent = newHashMap();
		new OutputTemplate(output).execute(new OutputTemplate.Hook() {

			@Override
			public void connected(final Session session, final Transport transport) throws MessagingException {
				int count = -1;
				for (final NewMailImpl element : elements) {
					try {
						logger.debug("sending e-mail '{}'", element);
						count++;
						final MessageBuilder messageBuilder = new NewMailImplMessageBuilder(output, session, element);
						final Message message = messageBuilder.build();
						transport.sendMessage(message, message.getAllRecipients());
						callback.sent(count);
						sent.put(message, element);
					} catch (final MessagingException e) {
						logger.error("error sending e-mail", e);
						if (!forgiving) {
							throw e;
						}
					} catch (final Exception e) {
						logger.error("error sending e-mail", e);
						if (!forgiving) {
							throw new MessagingException("error sending e-mail", e);
						}
					}
				}
			}

		});
		return sent;
	}

	private void store(final Map<Message, NewMail> sent) {
		try {
			if (isNotBlank(output.getOutputFolder()) && !sent.isEmpty()) {
				new InputTemplate(input).execute(new InputTemplate.Hook() {

					@Override
					public void connected(final Store store) throws MessagingException {
						for (final Entry<Message, NewMail> entry : sent.entrySet()) {
							try {
								final Message message = entry.getKey();
								final NewMail newMail = entry.getValue();
								logger.error("storing e-mail '{}'", newMail);
								final Folder folder = store.getFolder(output.getOutputFolder());
								if (!folder.exists()) {
									folder.create(Folder.HOLDS_MESSAGES);
								}
								folder.open(Folder.READ_WRITE);
								folder.appendMessages(new Message[] { message });
								message.setFlag(Flags.Flag.RECENT, true);
							} catch (final MessagingException e) {
								logger.error("error storing e-mail", e);
								if (!forgiving) {
									throw e;
								}
							} catch (final Exception e) {
								logger.error("error storing e-mail", e);
								if (!forgiving) {
									throw new MessagingException("error storing e-mail", e);
								}
							}
						}
					}

				});
			}
		} catch (final Exception e) {
			logger.error("error storing e-mail", e);
		}
	}

}
