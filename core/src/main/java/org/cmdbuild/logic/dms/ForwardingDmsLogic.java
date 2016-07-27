package org.cmdbuild.logic.dms;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.exception.DmsError;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;

public abstract class ForwardingDmsLogic extends ForwardingObject implements DmsLogic {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingDmsLogic() {
	}

	@Override
	protected abstract DmsLogic delegate();

	@Override
	public String getCategoryLookupType() {
		return delegate().getCategoryLookupType();
	}

	@Override
	public DocumentTypeDefinition getCategoryDefinition(final String category) {
		return delegate().getCategoryDefinition(category);
	}

	@Override
	public Iterable<DocumentTypeDefinition> getConfiguredCategoryDefinitions() {
		return delegate().getConfiguredCategoryDefinitions();
	}

	@Override
	public Iterable<DocumentTypeDefinition> getCategoryDefinitions() throws DmsError {
		return delegate().getCategoryDefinitions();
	}

	@Override
	public Map<String, Map<String, String>> getAutoCompletionRulesByClass(final String classname) throws DmsException {
		return delegate().getAutoCompletionRulesByClass(classname);
	}

	@Override
	public List<StoredDocument> search(final String className, final Long cardId) {
		return delegate().search(className, cardId);
	}

	@Override
	public Optional<StoredDocument> search(final String className, final Long cardId, final String fileName) {
		return delegate().search(className, cardId, fileName);
	}

	@Override
	public void upload(final String author, final String className, final Long cardId, final InputStream inputStream,
			final String fileName, final String category, final String description,
			final Iterable<MetadataGroup> metadataGroups) throws IOException, CMDBException {
		delegate().upload(author, className, cardId, inputStream, fileName, category, description, metadataGroups);
	}

	@Override
	public DataHandler download(final String className, final Long cardId, final String fileName) {
		return delegate().download(className, cardId, fileName);
	}

	@Override
	public void delete(final String className, final Long cardId, final String fileName) throws DmsException {
		delegate().delete(className, cardId, fileName);
	}

	@Override
	public void updateDescriptionAndMetadata(final String author, final String className, final Long cardId,
			final String filename, final String category, final String description,
			final Iterable<MetadataGroup> metadataGroups) {
		delegate().updateDescriptionAndMetadata(author, className, cardId, filename, category, description,
				metadataGroups);
	}

	@Override
	public void copy(final String sourceClassName, final Long sourceId, final String filename,
			final String destinationClassName, final Long destinationId) {
		delegate().copy(sourceClassName, sourceId, filename, destinationClassName, destinationId);
	}

	@Override
	public void move(final String sourceClassName, final Long sourceId, final String filename,
			final String destinationClassName, final Long destinationId) {
		delegate().move(sourceClassName, sourceId, filename, destinationClassName, destinationId);
	}

	@Override
	public Map<String, String> presets() {
		return delegate().presets();
	}

}
