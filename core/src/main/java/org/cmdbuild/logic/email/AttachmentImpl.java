package org.cmdbuild.logic.email;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.logic.email.EmailAttachmentsLogic.Attachment;

public class AttachmentImpl implements Attachment {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AttachmentImpl> {

		private String className;
		private Long cardId;
		private String fileName;

		private Builder() {
			// prevents instantiation
		}

		@Override
		public AttachmentImpl build() {
			return new AttachmentImpl(this);
		}

		public Builder withClassName(final String className) {
			this.className = className;
			return this;
		}

		public Builder withCardId(final Long cardId) {
			this.cardId = cardId;
			return this;
		}

		public Builder withFileName(final String fileName) {
			this.fileName = fileName;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String className;
	private final Long cardId;
	private final String fileName;

	private AttachmentImpl(final Builder builder) {
		this.className = builder.className;
		this.cardId = builder.cardId;
		this.fileName = builder.fileName;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public Long getCardId() {
		return cardId;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Attachment)) {
			return false;
		}
		final Attachment other = Attachment.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getClassName(), other.getClassName()) //
				.append(this.getCardId(), other.getCardId()) //
				.append(this.getFileName(), other.getFileName()) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(className) //
				.append(cardId) //
				.append(fileName) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}