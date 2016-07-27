package org.cmdbuild.common.api.mail;

import java.net.URL;

public interface SendableNewMail extends NewMail {

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withFrom(String from);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withTo(String to);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withTo(String... tos);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withTo(Iterable<String> tos);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withCc(String cc);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withCc(String... ccs);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withCc(Iterable<String> ccs);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withBcc(String bcc);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withBcc(String... bccs);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withBcc(Iterable<String> bccs);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withSubject(String subject);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withContent(String content);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	SendableNewMail withContentType(String contentType);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler)} instead.
	 */
	@Deprecated
	@Override
	SendableNewMail withAttachment(URL url);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler, String)} instead.
	 */
	@Deprecated
	@Override
	SendableNewMail withAttachment(URL url, String name);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler)} instead.
	 */
	@Deprecated
	@Override
	SendableNewMail withAttachment(String url);

	/**
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler, String)} instead.
	 */
	@Deprecated
	@Override
	SendableNewMail withAttachment(String url, String name);

	/**
	 * Sets if the mail will be sent asynchronously or not.
	 * 
	 * @param asyncronous
	 *            {@code true} if the mail must be sent asynchronously,
	 *            {@code false} otherwise.
	 * 
	 * @return a {@link SendableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	SendableNewMail withAsynchronousSend(boolean asynchronous);

	/**
	 * Sends the mail.
	 */
	void send();

}