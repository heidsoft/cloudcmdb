package org.cmdbuild.common.api.mail.javax.mail;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.api.mail.javax.mail.Constants.CONTENT_TYPE_TEXT_PLAIN;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.cmdbuild.common.api.mail.Configuration.Output;
import org.cmdbuild.common.api.mail.MailException;

class NewMailImplMessageBuilder implements MessageBuilder {

	private static final String MULTIPART_TYPE_WHEN_ATTACHMENTS = "mixed";

	private final Output configuration;
	private final Session session;
	private final NewMailImpl mail;

	public NewMailImplMessageBuilder(final Output configuration, final Session session, final NewMailImpl mail) {
		this.configuration = configuration;
		this.session = session;
		this.mail = mail;
	}

	@Override
	public Message build() {
		final Message message = new MimeMessage(session);
		fillMessage(message);
		return message;
	}

	private void fillMessage(final Message message) {
		try {
			setFrom(message);
			addRecipients(message);
			setSubject(message);
			setSentDate(message);
			setBody(message);
		} catch (final MessagingException e) {
			throw MailException.creation(e);
		}
	}

	private void setFrom(final Message message) throws MessagingException {
		final Collection<Address> addresses = newArrayList();
		final Iterable<String> fromsSource = mail.getFroms().isEmpty() ? configuration.getOutputFromRecipients() : mail
				.getFroms();
		for (final String address : fromsSource) {
			addresses.add(new InternetAddress(address));
		}
		message.addFrom(addresses.toArray(new Address[addresses.size()]));
	}

	private void addRecipients(final Message message) throws MessagingException {
		for (final RecipientType type : asList(RecipientType.TO, RecipientType.CC, RecipientType.BCC)) {
			for (final String recipient : mail.getRecipients().get(type)) {
				final Address address = new InternetAddress(recipient);
				message.addRecipient(type, address);
			}
		}
	}

	private void setSubject(final Message message) throws MessagingException {
		message.setSubject(mail.getSubject());
	}

	private void setSentDate(final Message message) throws MessagingException {
		message.setSentDate(Calendar.getInstance().getTime());
	}

	private void setBody(final Message message) throws MessagingException {
		final Part part;
		if ((isBlank(mail.getContentType()) || CONTENT_TYPE_TEXT_PLAIN.equals(mail.getContentType()))
				&& !hasAttachments()) {
			part = message;
			part.setText(defaultString(mail.getContent()));
		} else {
			final Multipart mp = new MimeMultipart(MULTIPART_TYPE_WHEN_ATTACHMENTS);
			part = new MimeBodyPart();
			part.setContent(defaultString(mail.getContent()), mail.getContentType());
			mp.addBodyPart((MimeBodyPart) part);
			if (hasAttachments()) {
				addAttachmentBodyParts(mp);
			}
			message.setContent(mp);
		}

	}

	private boolean hasAttachments() {
		return !mail.getAttachments().isEmpty();
	}

	private void addAttachmentBodyParts(final Multipart multipart) {
		try {
			for (final Entry<DataHandler, String> attachment : mail.getAttachments().entrySet()) {
				final BodyPart bodyPart = getBodyPartFor(attachment.getKey(), attachment.getValue());
				multipart.addBodyPart(bodyPart);
			}
		} catch (final MessagingException e) {
			throw MailException.creation(e);
		}
	}

	private BodyPart getBodyPartFor(final DataHandler dataHandler, final String name) throws MessagingException {
		final BodyPart bodyPart = new MimeBodyPart();
		bodyPart.setDataHandler(dataHandler);
		bodyPart.setFileName((name == null) ? getFileName(dataHandler.getName()) : name);
		return bodyPart;
	}

	private String getFileName(String name) {
		final String[] dirs = name.split("/");
		if (dirs.length > 0) {
			name = dirs[dirs.length - 1];
		}
		return name;
	}

}
