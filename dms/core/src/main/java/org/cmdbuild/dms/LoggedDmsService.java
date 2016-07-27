package org.cmdbuild.dms;

import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.exception.DmsError;

public class LoggedDmsService extends ForwardingDmsService implements LoggingSupport {

	private final DmsService delegate;

	public LoggedDmsService(final DmsService delegate) {
		this.delegate = delegate;
	}

	@Override
	protected DmsService delegate() {
		return delegate;
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError {
		logger.info("getting document type definitions");
		return super.getTypeDefinitions();
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
		logger.info("fetching stored documents");
		return super.search(document);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsError {
		logger.info("storing document");
		super.upload(document);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsError {
		logger.info("retrieving document");
		return super.download(document);
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsError {
		logger.info("deleting document");
		super.delete(document);
	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsError {
		logger.info("updating description and/or metadata");
		super.updateDescriptionAndMetadata(document);
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() throws DmsError {
		logger.info("getting autocompletion rules");
		return super.getAutoCompletionRules();
	}

	@Override
	public void clearCache() {
		logger.info("clearing internal cache");
		super.clearCache();
	}

	@Override
	public Map<String, String> getPresets() {
		logger.info("getting presets");
		return super.getPresets();
	}

}
