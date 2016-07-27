package org.cmdbuild.common.api.mail.javax.mail;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.cmdbuild.common.api.mail.GetMail;
import org.cmdbuild.common.utils.guava.Functions;

class GetMailImpl implements GetMail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<GetMailImpl> {

		private String id;
		private String folder;
		private String subject;
		private String from;
		private final List<String> tos = newArrayList();
		private final List<String> ccs = newArrayList();
		private String content;
		private final List<Attachment> attachments = newArrayList();

		private Builder() {
			// prevents instantiation
		}

		@Override
		public GetMailImpl build() {
			return new GetMailImpl(this);
		}

		public Builder withId(final String id) {
			this.id = id;
			return this;
		}

		public Builder withFolder(final String folder) {
			this.folder = folder;
			return this;
		}

		public Builder withSubject(final String subject) {
			this.subject = subject;
			return this;
		}

		public Builder withFrom(final String from) {
			this.from = from;
			return this;
		}

		public Builder withTos(final Iterable<String> tos) {
			addAll(this.tos, trim(tos));
			return this;
		}

		public Builder withCcs(final Iterable<String> ccs) {
			addAll(this.ccs, trim(ccs));
			return this;
		}

		private Iterable<? extends String> trim(final Iterable<String> elements) {
			return from(elements).transform(Functions.trim());
		}

		public Builder withContent(final String content) {
			this.content = content;
			return this;
		}

		public Builder withAttachments(final Iterable<Attachment> attachments) {
			this.attachments.addAll(newArrayList(attachments));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String id;
	private final String folder;
	private final String subject;
	private final String from;
	private final Iterable<String> tos;
	private final Iterable<String> ccs;
	private final String content;
	private final Iterable<Attachment> attachments;

	public GetMailImpl(final Builder builder) {
		this.id = builder.id;
		this.folder = builder.folder;
		this.subject = builder.subject;
		this.from = builder.from;
		this.tos = builder.tos;
		this.ccs = builder.ccs;
		this.content = builder.content;
		this.attachments = builder.attachments;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getFolder() {
		return folder;
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public String getFrom() {
		return from;
	}

	@Override
	public Iterable<String> getTos() {
		return tos;
	}

	@Override
	public Iterable<String> getCcs() {
		return ccs;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public Iterable<Attachment> getAttachments() {
		return attachments;
	}

}
