package org.cmdbuild.common.api.mail.javax.mail;

import org.cmdbuild.common.api.mail.FetchedMail;

class FetchedMailImpl implements FetchedMail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<FetchedMailImpl> {

		private String id;
		private String folder;
		private String subject;

		private Builder() {
			// prevents instantiation
		}

		@Override
		public FetchedMailImpl build() {
			return new FetchedMailImpl(this);
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

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String id;
	private final String folder;
	private final String subject;

	public FetchedMailImpl(final Builder builder) {
		this.id = builder.id;
		this.folder = builder.folder;
		this.subject = builder.subject;
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

}
