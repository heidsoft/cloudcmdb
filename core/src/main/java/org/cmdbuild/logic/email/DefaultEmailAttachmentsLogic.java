package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.logic.email.EmailLogic.Email;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class DefaultEmailAttachmentsLogic implements EmailAttachmentsLogic {

	private static final String CATEGORY_NOT_USED = null;

	private final CMDataView dataView;
	private final DmsService dmsService;
	private final DocumentCreatorFactory documentCreatorFactory;
	private final OperationUser operationUser;

	public DefaultEmailAttachmentsLogic( //
			final CMDataView dataView, //
			final DmsService dmsService, //
			final DocumentCreatorFactory documentCreatorFactory, //
			final OperationUser operationUser //
	) {
		this.dataView = dataView;
		this.dmsService = dmsService;
		this.documentCreatorFactory = documentCreatorFactory;
		this.operationUser = operationUser;
	}

	@Override
	public void upload(final Email email, final DataHandler dataHandler) throws CMDBException {
		InputStream inputStream = null;
		try {
			inputStream = dataHandler.getInputStream();
			final StorableDocument document = documentCreator(email.isTemporary()) //
					.createStorableDocument( //
							operationUser.getAuthenticatedUser().getUsername(), //
							EMAIL_CLASS_NAME, //
							email.getId(), //
							inputStream, //
							dataHandler.getName(), //
							CATEGORY_NOT_USED, //
							EMPTY);
			dmsService.upload(document);
		} catch (final Exception e) {
			logger.error("error uploading document");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	@Override
	public void copy(final Email email, final Attachment attachment) throws CMDBException {
		try {
			final DocumentSearch _destination = documentCreator(email.isTemporary()) //
					.createDocumentSearch(EMAIL_CLASS_NAME, email.getId());
			dmsService.create(_destination);
			final CMClass sourceClass = dataView.findClass(attachment.getClassName());
			final DocumentSearch _source = documentCreatorFactory.create(sourceClass) //
					.createDocumentSearch(attachment.getClassName(), attachment.getCardId());
			for (final StoredDocument storedDocument : dmsService.search(_source)) {
				if (storedDocument.getName().equals(attachment.getFileName())) {
					dmsService.copy(storedDocument, _source, _destination);
				}
			}
		} catch (final Exception e) {
			logger.error("error copying document");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		}
	}

	@Override
	public void copyAll(final Email source, final Email destination) throws CMDBException {
		try {
			final DocumentSearch _source = documentCreator(source.isTemporary()) //
					.createDocumentSearch(EMAIL_CLASS_NAME, source.getId());
			final DocumentSearch _destination = documentCreator(destination.isTemporary()) //
					.createDocumentSearch(EMAIL_CLASS_NAME, destination.getId());
			dmsService.create(_destination);
			for (final StoredDocument storedDocument : dmsService.search(_source)) {
				dmsService.copy(storedDocument, _source, _destination);
			}
		} catch (final Exception e) {
			logger.error("error copying documents");
			throw DmsException.Type.DMS_ATTACHMENT_UPLOAD_ERROR.createException();
		}
	}

	private DocumentCreator documentCreator(final boolean temporary) {
		final DocumentCreator documentCreator;
		if (temporary) {
			documentCreator = documentCreatorFactory.createTemporary(Arrays.asList(EMAIL_CLASS_NAME));
		} else {
			final CMClass emailClass = dataView.findClass(EMAIL_CLASS_NAME);
			documentCreator = documentCreatorFactory.create(emailClass);
		}
		return documentCreator;
	}

	@Override
	public Iterable<Attachment> readAll(final Email email) throws CMDBException {
		try {
			final DocumentSearch destination = documentCreator(email.isTemporary()) //
					.createDocumentSearch(EMAIL_CLASS_NAME, email.getId());
			dmsService.create(destination);
			final List<StoredDocument> documents = dmsService.search(destination);
			return from(documents) //
					.transform(new Function<StoredDocument, Attachment>() {

						@Override
						public Attachment apply(final StoredDocument input) {
							return AttachmentImpl.newInstance() //
									.withClassName(EMAIL_CLASS_NAME) //
									.withCardId(email.getId()) //
									.withFileName(input.getName()) //
									.build();
						}

					});
		} catch (final Exception e) {
			logger.error("error reading documents");
			throw DmsException.Type.DMS_ATTACHMENT_NOTFOUND.createException();
		}

	}

	@Override
	public Optional<DataHandler> read(final Email email, final Attachment attachment) throws CMDBException {
		try {
			final DocumentCreator documentCreator = documentCreator(email.isTemporary());
			final DocumentSearch target = documentCreator.createDocumentSearch(EMAIL_CLASS_NAME, email.getId());
			Optional<DataHandler> dataHandler = Optional.absent();
			for (final StoredDocument storedDocument : dmsService.search(target)) {
				if (storedDocument.getName().equals(attachment.getFileName())) {
					logger.debug("downloading attachment with name '{}'", storedDocument.getName());
					final DocumentDownload document = documentCreator.createDocumentDownload( //
							EMAIL_CLASS_NAME, //
							email.getId(), //
							storedDocument.getName());
					dataHandler = Optional.of(dmsService.download(document));
					break;
				}
			}
			return dataHandler;
		} catch (final DmsError e) {
			logger.error("error reading document");
			throw DmsException.Type.DMS_ATTACHMENT_NOTFOUND.createException();
		}
	}

	@Override
	public void delete(final Email email, final Attachment attachment) throws CMDBException {
		try {
			final DocumentDelete document = documentCreator(email.isTemporary()) //
					.createDocumentDelete(EMAIL_CLASS_NAME, email.getId(), attachment.getFileName());
			dmsService.delete(document);
		} catch (final Exception e) {
			logger.error("error deleting document");
			throw DmsException.Type.DMS_ATTACHMENT_DELETE_ERROR.createException();
		}
	}

	@Override
	public DataHandler download(final Email email, final Attachment attachment) {
		try {
			final DocumentDownload document = documentCreator(email.isTemporary()) //
					.createDocumentDownload(EMAIL_CLASS_NAME, email.getId(), attachment.getFileName());
			return dmsService.download(document);
		} catch (final Exception e) {
			logger.error("error downloading document");
			throw DmsException.Type.DMS_ATTACHMENT_NOTFOUND.createException();
		}
	}

}
