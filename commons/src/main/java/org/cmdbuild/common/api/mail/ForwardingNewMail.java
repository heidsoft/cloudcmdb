package org.cmdbuild.common.api.mail;

import java.net.URL;

import javax.activation.DataHandler;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingNewMail extends ForwardingObject implements NewMail {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingNewMail() {
	}

	@Override
	protected abstract NewMail delegate();

	@Override
	public NewMail withFrom(final String from) {
		return delegate().withFrom(from);
	}

	@Override
	public NewMail withTo(final String to) {
		return delegate().withTo(to);
	}

	@Override
	public NewMail withTo(final String... tos) {
		return delegate().withTo(tos);
	}

	@Override
	public NewMail withTo(final Iterable<String> tos) {
		return delegate().withTo(tos);
	}

	@Override
	public NewMail withCc(final String cc) {
		return delegate().withCc(cc);
	}

	@Override
	public NewMail withCc(final String... ccs) {
		return delegate().withCc(ccs);
	}

	@Override
	public NewMail withCc(final Iterable<String> ccs) {
		return delegate().withCc(ccs);
	}

	@Override
	public NewMail withBcc(final String bcc) {
		return delegate().withBcc(bcc);
	}

	@Override
	public NewMail withBcc(final String... bccs) {
		return delegate().withBcc(bccs);
	}

	@Override
	public NewMail withBcc(final Iterable<String> bccs) {
		return delegate().withBcc(bccs);
	}

	@Override
	public NewMail withSubject(final String subject) {
		return delegate().withSubject(subject);
	}

	@Override
	public NewMail withContent(final String content) {
		return delegate().withContent(content);
	}

	@Override
	public NewMail withContentType(final String contentType) {
		return delegate().withContentType(contentType);
	}

	@Override
	public NewMail withAttachment(final URL url) {
		return delegate().withAttachment(url);
	}

	@Override
	public NewMail withAttachment(final URL url, final String name) {
		return delegate().withAttachment(url, name);
	}

	@Override
	public NewMail withAttachment(final String url) {
		return delegate().withAttachment(url);
	}

	@Override
	public NewMail withAttachment(final String url, final String name) {
		return delegate().withAttachment(url, name);
	}

	@Override
	public NewMail withAttachment(final DataHandler dataHandler) {
		return delegate().withAttachment(dataHandler);
	}

	@Override
	public NewMail withAttachment(final DataHandler dataHandler, final String name) {
		return delegate().withAttachment(dataHandler, name);
	}

}
