package org.cmdbuild.logic.email;

import javax.activation.DataHandler;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.email.EmailLogic.Email;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;

public interface EmailAttachmentsLogic extends Logic {

	interface Attachment {

		String getClassName();

		Long getCardId();

		String getFileName();

	}

	abstract class ForwardingAttachment extends ForwardingObject implements Attachment {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingAttachment() {
		}

		@Override
		protected abstract Attachment delegate();

		@Override
		public String getClassName() {
			return delegate().getClassName();
		}

		@Override
		public Long getCardId() {
			return delegate().getCardId();
		}

		@Override
		public String getFileName() {
			return delegate().getFileName();
		}

	}

	abstract class ForwardingEmailAttachmentsLogic extends ForwardingObject implements EmailAttachmentsLogic {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingEmailAttachmentsLogic() {
		}

		@Override
		protected abstract EmailAttachmentsLogic delegate();

		@Override
		public void upload(final Email email, final DataHandler dataHandler) throws CMDBException {
			delegate().upload(email, dataHandler);
		}

		@Override
		public void copy(final Email email, final Attachment attachment) throws CMDBException {
			delegate().copy(email, attachment);
		}

		@Override
		public void copyAll(final Email source, final Email destination) throws CMDBException {
			delegate().copyAll(source, destination);
		}

		@Override
		public Iterable<Attachment> readAll(final Email email) throws CMDBException {
			return delegate().readAll(email);
		}

		@Override
		public Optional<DataHandler> read(final Email email, final Attachment attachment) throws CMDBException {
			return delegate().read(email, attachment);
		}

		@Override
		public void delete(final Email email, final Attachment attachment) throws CMDBException {
			delegate().delete(email, attachment);
		}

		@Override
		public DataHandler download(final Email email, final Attachment attachment) {
			return delegate().download(email, attachment);
		}

	}

	void upload(Email email, DataHandler dataHandler) throws CMDBException;

	void copy(Email email, Attachment attachment) throws CMDBException;

	void copyAll(Email source, Email destination) throws CMDBException;

	Iterable<Attachment> readAll(Email email) throws CMDBException;

	Optional<DataHandler> read(Email email, Attachment attachment) throws CMDBException;

	void delete(Email email, Attachment attachment) throws CMDBException;

	DataHandler download(Email email, Attachment attachment);

}
