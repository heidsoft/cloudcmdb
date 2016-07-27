package org.cmdbuild.common.api.mail.javax.mail;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.CONTENT_TYPE_TEXT_PLAIN;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Message.RecipientType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.common.api.mail.NewMail;
import org.slf4j.Logger;

class NewMailImpl implements NewMail {

	private final Logger logger;

	private final Collection<String> froms;
	private final Map<RecipientType, Collection<String>> recipients;
	private String subject;
	private String content;
	private String contentType;
	private final Map<DataHandler, String> attachments;

	public NewMailImpl(final Logger logger) {
		this.logger = logger;
		this.froms = newLinkedHashSet();
		this.recipients = newHashMap();
		this.recipients.put(TO, newStringSet());
		this.recipients.put(CC, newStringSet());
		this.recipients.put(BCC, newStringSet());
		this.contentType = CONTENT_TYPE_TEXT_PLAIN;
		this.attachments = newHashMap();
	}

	private Set<String> newStringSet() {
		return newLinkedHashSet();
	}

	@Override
	public NewMail withFrom(final String from) {
		froms.add(trim(from));
		return this;
	}

	public Collection<String> getFroms() {
		return froms;
	}

	@Override
	public NewMail withTo(final String to) {
		addRecipient(TO, to);
		return this;
	}

	@Override
	public NewMail withTo(final String... tos) {
		return withTo(asList(tos));
	}

	@Override
	public NewMail withTo(final Iterable<String> tos) {
		addRecipients(TO, tos);
		return this;
	}

	@Override
	public NewMail withCc(final String cc) {
		addRecipient(CC, cc);
		return this;
	}

	@Override
	public NewMail withCc(final String... ccs) {
		return withCc(asList(ccs));
	}

	@Override
	public NewMail withCc(final Iterable<String> ccs) {
		addRecipients(CC, ccs);
		return this;
	}

	@Override
	public NewMail withBcc(final String bcc) {
		addRecipient(BCC, bcc);
		return this;
	}

	@Override
	public NewMail withBcc(final String... bccs) {
		return withBcc(asList(bccs));
	}

	@Override
	public NewMail withBcc(final Iterable<String> bccs) {
		addRecipients(BCC, bccs);
		return this;
	}

	private void addRecipients(final RecipientType type, final Iterable<String> recipients) {
		if (recipients != null) {
			for (final String recipient : recipients) {
				addRecipient(type, recipient);
			}
		}
	}

	private void addRecipient(final RecipientType type, final String recipient) {
		if (isBlank(recipient)) {
			logger.info("invalid recipient {} '{}', will not be added", type.getClass().getSimpleName(), recipient);
		} else {
			recipients.get(type).add(trim(recipient));
		}
	}

	public Map<RecipientType, Collection<String>> getRecipients() {
		return recipients;
	}

	@Override
	public NewMail withSubject(final String subject) {
		this.subject = subject;
		return this;
	}

	public String getSubject() {
		return subject;
	}

	@Override
	public NewMail withContent(final String body) {
		this.content = body;
		return this;
	}

	public String getContent() {
		return content;
	}

	@Override
	public NewMail withContentType(final String contentType) {
		this.contentType = contentType;
		return this;
	}

	public String getContentType() {
		return contentType;
	}

	@Override
	public NewMail withAttachment(final URL url) {
		return withAttachment(url, null);
	}

	@Override
	public NewMail withAttachment(final URL url, final String name) {
		return withAttachment(new DataHandler(new URLDataSource(url)), name);
	}

	@Override
	public NewMail withAttachment(final String url) {
		return withAttachment(url, null);
	}

	@Override
	public NewMail withAttachment(final String url, final String name) {
		try {
			return withAttachment(new URL(url), name);
		} catch (final MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public NewMail withAttachment(final DataHandler dataHandler) {
		return withAttachment(dataHandler, null);
	}

	@Override
	public NewMail withAttachment(final DataHandler dataHandler, final String name) {
		attachments.put(dataHandler, name);
		return this;
	}

	public Map<DataHandler, String> getAttachments() {
		return attachments;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE) //
				.append(froms) //
				.append(recipients) //
				.append(subject) //
				.append(content) //
				.append(contentType) //
				.append(attachments.values()) //
				.toString();
	}

}
