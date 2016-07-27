package org.cmdbuild.common.api.mail;

import java.net.URL;

import javax.activation.DataHandler;

public interface QueueableNewMail extends NewMail {

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withFrom(String from);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withTo(String to);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withTo(String... tos);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withTo(Iterable<String> tos);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withCc(String cc);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withCc(String... ccs);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withCc(Iterable<String> ccs);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withBcc(String bcc);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withBcc(String... bccs);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withBcc(Iterable<String> bccs);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withSubject(String subject);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withContent(String content);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withContentType(String contentType);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler)} instead.
	 */
	@Deprecated
	@Override
	QueueableNewMail withAttachment(URL url);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler, String)} instead.
	 */
	@Deprecated
	@Override
	QueueableNewMail withAttachment(URL url, String name);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler)} instead.
	 */
	@Deprecated
	@Override
	QueueableNewMail withAttachment(String url);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 * 
	 * @deprecated use {@link withAttachment(DataHandler, String)} instead.
	 */
	@Deprecated
	@Override
	QueueableNewMail withAttachment(String url, String name);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withAttachment(DataHandler dataHandler);

	/**
	 * @return a {@link QueueableNewMail} object, can be {@code this} or a new
	 *         instance.
	 */
	@Override
	QueueableNewMail withAttachment(DataHandler dataHandler, String name);

	/**
	 * Queues the new mail.
	 */
	NewMailQueue add();

}
