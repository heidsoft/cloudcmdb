package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.size;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.v1.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;

import java.util.NoSuchElementException;

import javax.activation.DataHandler;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.v1.model.Attachment;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfCardAttachments implements AllInOneCardAttachments {

	private final DataAccessLogic dataAccessLogic;
	private final ErrorHandler errorHandler;
	private final AttachmentsHelper attachmentsHelper;

	public CxfCardAttachments(final ErrorHandler errorHandler, final DataAccessLogic dataAccessLogic,
			final AttachmentsHelper attachmentsHelper) {
		this.errorHandler = errorHandler;
		this.dataAccessLogic = dataAccessLogic;
		this.attachmentsHelper = attachmentsHelper;
	}

	@Override
	public ResponseSingle<String> create(final String classId, final Long cardId, final Attachment attachment,
			final DataHandler dataHandler) {
		checkPreconditions(classId, cardId);
		if (dataHandler == null) {
			errorHandler.missingFile();
		}
		if (isBlank(dataHandler.getName())) {
			errorHandler.missingAttachmentName();
		}
		if (from(attachmentsHelper.search(classId, cardId)) //
				.filter(nameEquals(dataHandler.getName())) //
				.first() //
				.isPresent()) {
			errorHandler.alreadyExistingAttachmentName(dataHandler.getName());
		}
		try {
			final String id = attachmentsHelper.create(classId, cardId, dataHandler.getName(), attachment, dataHandler);
			return newResponseSingle(String.class) //
					.withElement(id) //
					.build();
		} catch (final Exception e) {
			errorHandler.propagate(e);
			assert false : "should not come here";
			return null;
		}
	}

	@Override
	public ResponseMultiple<Attachment> read(final String classId, final Long cardId) {
		checkPreconditions(classId, cardId);
		final Iterable<Attachment> elements = attachmentsHelper.search(classId, cardId);
		return newResponseMultiple(Attachment.class) //
				.withElements(elements) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(size(elements))) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<Attachment> read(final String classId, final Long cardId, final String attachmentId) {
		checkPreconditions(classId, cardId, attachmentId);
		final Optional<Attachment> element = attachmentsHelper.search(classId, cardId, attachmentId);
		if (!element.isPresent()) {
			errorHandler.attachmentNotFound(attachmentId);
		}
		return newResponseSingle(Attachment.class) //
				.withElement(element.get()) //
				.build();
	}

	@Override
	public DataHandler download(final String classId, final Long cardId, final String attachmentId) {
		checkPreconditions(classId, cardId);
		return attachmentsHelper.download(classId, cardId, attachmentId);
	}

	@Override
	public void update(final String classId, final Long cardId, final String attachmentId, final Attachment attachment,
			final DataHandler dataHandler) {
		checkPreconditions(classId, cardId);
		if (isBlank(attachmentId)) {
			errorHandler.missingAttachmentId();
		}
		if ((dataHandler != null) && !from(attachmentsHelper.search(classId, cardId)) //
				.filter(nameEquals(dataHandler.getName())) //
				.first() //
				.isPresent()) {
			errorHandler.differentAttachmentName(dataHandler.getName());
		}
		try {
			attachmentsHelper.update(classId, cardId, attachmentId, attachment, dataHandler);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
	}

	@Override
	public void delete(final String classId, final Long cardId, final String attachmentId) {
		checkPreconditions(classId, cardId);
		attachmentsHelper.delete(classId, cardId, attachmentId);
	}

	private void checkPreconditions(final String classId, final Long cardId) {
		final CMClass targetClass = dataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		try {
			dataAccessLogic.fetchCard(classId, cardId);
		} catch (final NoSuchElementException e) {
			errorHandler.cardNotFound(cardId);
		}
	}

	private void checkPreconditions(final String classId, final Long cardId, final String attachmentId) {
		final CMClass targetClass = dataAccessLogic.findClass(classId);
		if (targetClass == null) {
			errorHandler.classNotFound(classId);
		}
		try {
			dataAccessLogic.fetchCard(classId, cardId);
		} catch (final NoSuchElementException e) {
			errorHandler.cardNotFound(cardId);
		}
		if (isBlank(attachmentId)) {
			errorHandler.missingAttachmentId();
		}
	}

	private Predicate<Attachment> nameEquals(final String fileName) {
		return new Predicate<Attachment>() {

			@Override
			public boolean apply(final Attachment input) {
				return input.getName().equals(fileName);
			}

		};
	}

}
