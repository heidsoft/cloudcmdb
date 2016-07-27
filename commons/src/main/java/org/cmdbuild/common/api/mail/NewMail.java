package org.cmdbuild.common.api.mail;

import java.net.URL;

import javax.activation.DataHandler;

/**
 * New mail interface.
 */
public interface NewMail {

	/**
	 * Adds a FROM recipient.
	 * 
	 * @param from
	 *            is the mail address of a FROM recipient.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withFrom(String from);

	/**
	 * Adds a TO recipient.
	 * 
	 * @param to
	 *            is the mail address of a TO recipient.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withTo(String to);

	/**
	 * Adds some TO recipients.
	 * 
	 * @param tos
	 *            all the TO recipients.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withTo(String... tos);

	/**
	 * Adds some TO recipients.
	 * 
	 * @param tos
	 *            all the TO recipients.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withTo(Iterable<String> tos);

	/**
	 * Adds a CC recipient.
	 * 
	 * @param cc
	 *            is the mail address of a CC recipient.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withCc(String cc);

	/**
	 * Adds some CC recipients.
	 * 
	 * @param ccs
	 *            all the CC recipients.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withCc(String... ccs);

	/**
	 * Adds some CC recipients.
	 * 
	 * @param ccs
	 *            all the CC recipients.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withCc(Iterable<String> ccs);

	/**
	 * Adds a BCC recipient.
	 * 
	 * @param to
	 *            is the mail address of a BCC recipient.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withBcc(String bcc);

	/**
	 * Adds some BCC recipients.
	 * 
	 * @param bccs
	 *            all the BCC recipients.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withBcc(String... bccs);

	/**
	 * Adds some BCC recipients.
	 * 
	 * @param bccs
	 *            all the BCC recipients.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withBcc(Iterable<String> bccs);

	/**
	 * Sets the subject.
	 * 
	 * @param subject
	 *            is the subject of the mail.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withSubject(String subject);

	/**
	 * Sets the content.
	 * 
	 * @param content
	 *            is the content of the mail.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withContent(String content);

	/**
	 * Sets the content-type.
	 * 
	 * @param contentType
	 *            is the content-type od the body.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withContentType(String contentType);

	/**
	 * Adds an attachment.
	 * 
	 * @param url
	 *            is the {@link URL} of the attachment.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler)} instead.
	 */
	@Deprecated
	NewMail withAttachment(URL url);

	/**
	 * Adds an attachment with the specified name.
	 * 
	 * @param url
	 *            is the {@link URL} of the attachment.
	 * @param name
	 *            is the name of the attachment.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler, String)} instead.
	 */
	@Deprecated
	NewMail withAttachment(URL url, String name);

	/**
	 * @deprecated use {@link withAttachment(DataHandler)} instead.
	 */
	@Deprecated
	NewMail withAttachment(String url);

	/**
	 * @deprecated use {@link withAttachment(DataHandler, String)} instead.
	 */
	@Deprecated
	NewMail withAttachment(String url, String name);

	/**
	 * Adds an attachment.
	 * 
	 * @param dataHandler
	 *            is the {@link DataHandler} of the attachment.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withAttachment(DataHandler dataHandler);

	/**
	 * Adds an attachment with the specified name.
	 * 
	 * @param dataHandler
	 *            is the {@link DataHandler} of the attachment.
	 * @param name
	 *            is the name of the attachment.
	 * 
	 * @return a {@link NewMail} object, can be {@code this} or a new instance.
	 */
	NewMail withAttachment(DataHandler dataHandler, String name);

}