package org.cmdbuild.logic.email;

import org.cmdbuild.logic.Logic;
import org.joda.time.DateTime;

import com.google.common.collect.ForwardingObject;

public interface EmailLogic extends Logic {

	interface Status {

		void accept(StatusVisitor visitor);

	}

	interface StatusVisitor {

		void visit(Received status);

		void visit(Sent status);

		void visit(Outgoing status);

		void visit(Draft status);

	}

	class Received implements Status {

		private Received() {
			// use flyweight object
		}

		@Override
		public void accept(final StatusVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Draft implements Status {

		private Draft() {
			// use flyweight object
		}

		@Override
		public void accept(final StatusVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Outgoing implements Status {

		private Outgoing() {
			// use flyweight object
		}

		@Override
		public void accept(final StatusVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Sent implements Status {

		private Sent() {
			// use flyweight object
		}

		@Override
		public void accept(final StatusVisitor visitor) {
			visitor.visit(this);
		}

	}

	class Statuses {

		private static Status received = new Received();
		private static Status draft = new Draft();
		private static Status outgoing = new Outgoing();
		private static Status sent = new Sent();

		public static Status received() {
			return received;
		}

		public static Status draft() {
			return draft;
		}

		public static Status outgoing() {
			return outgoing;
		}

		public static Status sent() {
			return sent;
		}

	}

	interface Email {

		Long getId();

		String getFromAddress();

		String getToAddresses();

		String getCcAddresses();

		String getBccAddresses();

		String getSubject();

		String getContent();

		DateTime getDate();

		Status getStatus();

		Long getReference();

		String getNotifyWith();

		boolean isNoSubjectPrefix();

		String getAccount();

		boolean isTemporary();

		String getTemplate();

		boolean isKeepSynchronization();

		boolean isPromptSynchronization();

		long getDelay();

	}

	abstract class ForwardingEmail extends ForwardingObject implements Email {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingEmail() {
		}

		@Override
		protected abstract Email delegate();

		@Override
		public Long getId() {
			return delegate().getId();
		}

		@Override
		public String getFromAddress() {
			return delegate().getFromAddress();
		}

		@Override
		public String getToAddresses() {
			return delegate().getToAddresses();
		}

		@Override
		public String getCcAddresses() {
			return delegate().getCcAddresses();
		}

		@Override
		public String getBccAddresses() {
			return delegate().getBccAddresses();
		}

		@Override
		public String getSubject() {
			return delegate().getSubject();
		}

		@Override
		public String getContent() {
			return delegate().getContent();
		}

		@Override
		public DateTime getDate() {
			return delegate().getDate();
		}

		@Override
		public Status getStatus() {
			return delegate().getStatus();
		}

		@Override
		public Long getReference() {
			return delegate().getReference();
		}

		@Override
		public String getNotifyWith() {
			return delegate().getNotifyWith();
		}

		@Override
		public boolean isNoSubjectPrefix() {
			return delegate().isNoSubjectPrefix();
		}

		@Override
		public String getAccount() {
			return delegate().getAccount();
		}

		@Override
		public boolean isTemporary() {
			return delegate().isTemporary();
		}

		@Override
		public String getTemplate() {
			return delegate().getTemplate();
		}

		@Override
		public boolean isKeepSynchronization() {
			return delegate().isKeepSynchronization();
		}

		@Override
		public boolean isPromptSynchronization() {
			return delegate().isPromptSynchronization();
		}

		@Override
		public long getDelay() {
			return delegate().getDelay();
		}

	}

	boolean isEnabled(String className, Long cardId);

	Long create(Email email);

	Iterable<Email> readAll(Long reference);

	Iterable<Email> readAll(Status status);

	Email read(Email email);

	void update(Email email);

	void updateWithNoChecks(Email email);

	void delete(Email email);

	void deleteWithNoChecks(Email email);

}
