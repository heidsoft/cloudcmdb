package org.cmdbuild.logic.email;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.logic.email.EmailLogic.Email;
import org.cmdbuild.logic.email.EmailLogic.Status;
import org.joda.time.DateTime;

public class EmailImpl implements Email {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<Email> {

		private Long id;
		private String fromAddress;
		private String toAddresses;
		private String ccAddresses;
		private String bccAddresses;
		private String subject;
		private String content;
		private String notifyWith;
		private DateTime date;
		private Status status;
		private Long reference;
		private boolean noSubjectPrefix;
		private String account;
		private boolean temporary;
		private String template;
		private boolean keepSynchronization;
		private boolean promptSynchronization;
		private long delay;

		private Builder() {
			// use factory method
		}

		@Override
		public Email build() {
			return new EmailImpl(this);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withFromAddress(final String fromAddress) {
			this.fromAddress = fromAddress;
			return this;
		}

		public Builder withToAddresses(final String toAddresses) {
			this.toAddresses = toAddresses;
			return this;
		}

		public Builder withCcAddresses(final String ccAddresses) {
			this.ccAddresses = ccAddresses;
			return this;
		}

		public Builder withBccAddresses(final String bccAddresses) {
			this.bccAddresses = bccAddresses;
			return this;
		}

		public Builder withSubject(final String subject) {
			this.subject = subject;
			return this;
		}

		public Builder withContent(final String content) {
			this.content = content;
			return this;
		}

		public Builder withNotifyWith(final String notifyWith) {
			this.notifyWith = notifyWith;
			return this;
		}

		public Builder withDate(final DateTime date) {
			this.date = date;
			return this;
		}

		public Builder withStatus(final Status status) {
			this.status = status;
			return this;
		}

		public Builder withReference(final Long reference) {
			this.reference = reference;
			return this;
		}

		public Builder withNoSubjectPrefix(final boolean noSubjectPrefix) {
			this.noSubjectPrefix = noSubjectPrefix;
			return this;
		}

		public Builder withAccount(final String account) {
			this.account = account;
			return this;
		}

		public Builder withTemporary(final boolean temporary) {
			this.temporary = temporary;
			return this;
		}

		public Builder withTemplate(final String template) {
			this.template = template;
			return this;
		}

		public Builder withKeepSynchronization(final boolean keepSynchronization) {
			this.keepSynchronization = keepSynchronization;
			return this;
		}

		public Builder withPromptSynchronization(final boolean promptSynchronization) {
			this.promptSynchronization = promptSynchronization;
			return this;
		}

		public Builder withDelay(final long delay) {
			this.delay = delay;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String fromAddress;
	private final String toAddresses;
	private final String ccAddresses;
	private final String bccAddresses;
	private final String subject;
	private final String content;
	private final String notifyWith;
	private final DateTime date;
	private final Status status;
	private final Long reference;
	private final boolean noSubjectPrefix;
	private final String account;
	private final boolean temporary;
	private final String template;
	private final boolean keepSynchronization;
	private final boolean promptSynchronization;
	private final long delay;

	private EmailImpl(final Builder builder) {
		this.id = builder.id;
		this.fromAddress = builder.fromAddress;
		this.toAddresses = builder.toAddresses;
		this.ccAddresses = builder.ccAddresses;
		this.bccAddresses = builder.bccAddresses;
		this.subject = builder.subject;
		this.content = builder.content;
		this.notifyWith = builder.notifyWith;
		this.date = builder.date;
		this.status = builder.status;
		this.reference = builder.reference;
		this.noSubjectPrefix = builder.noSubjectPrefix;
		this.account = builder.account;
		this.temporary = builder.temporary;
		this.template = builder.template;
		this.keepSynchronization = builder.keepSynchronization;
		this.promptSynchronization = builder.promptSynchronization;
		this.delay = builder.delay;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getFromAddress() {
		return fromAddress;
	}

	@Override
	public String getToAddresses() {
		return toAddresses;
	}

	@Override
	public String getCcAddresses() {
		return ccAddresses;
	}

	@Override
	public String getBccAddresses() {
		return bccAddresses;
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public DateTime getDate() {
		return date;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public Long getReference() {
		return reference;
	}

	@Override
	public String getNotifyWith() {
		return notifyWith;
	}

	@Override
	public boolean isNoSubjectPrefix() {
		return noSubjectPrefix;
	}

	@Override
	public String getAccount() {
		return account;
	}

	@Override
	public boolean isTemporary() {
		return temporary;
	}

	@Override
	public String getTemplate() {
		return template;
	}

	@Override
	public boolean isKeepSynchronization() {
		return keepSynchronization;
	}

	@Override
	public boolean isPromptSynchronization() {
		return promptSynchronization;
	}

	@Override
	public long getDelay() {
		return delay;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Email)) {
			return false;
		}

		final Email other = Email.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.getFromAddress(), other.getFromAddress()) //
				.append(this.getToAddresses(), other.getToAddresses()) //
				.append(this.getCcAddresses(), other.getCcAddresses()) //
				.append(this.getBccAddresses(), other.getBccAddresses()) //
				.append(this.getSubject(), other.getSubject()) //
				.append(this.getContent(), other.getContent()) //
				.append(this.getDate(), other.getDate()) //
				.append(this.getStatus(), other.getStatus()) //
				.append(this.getReference(), other.getReference()) //
				.append(this.getNotifyWith(), other.getNotifyWith()) //
				.append(this.isNoSubjectPrefix(), other.isNoSubjectPrefix()) //
				.append(this.getAccount(), other.getAccount()) //
				.append(this.isTemporary(), other.isTemporary()) //
				.append(this.getTemplate(), other.getTemplate()) //
				.append(this.isKeepSynchronization(), other.isKeepSynchronization()) //
				.append(this.isPromptSynchronization(), other.isPromptSynchronization()) //
				.append(this.getDelay(), other.getDelay()) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(id) //
				.append(fromAddress) //
				.append(toAddresses) //
				.append(ccAddresses) //
				.append(bccAddresses) //
				.append(subject) //
				.append(content) //
				.append(date) //
				.append(status) //
				.append(reference) //
				.append(notifyWith) //
				.append(noSubjectPrefix) //
				.append(account) //
				.append(temporary) //
				.append(template) //
				.append(keepSynchronization) //
				.append(promptSynchronization) //
				.append(delay) //
				.toHashCode();
	}

	@Override
	public final String toString() {
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE).toString();
	}

}