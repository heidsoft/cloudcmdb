package org.cmdbuild.dms.alfresco;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Suppliers.synchronizedSupplier;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dms.MetadataAutocompletion.NULL_AUTOCOMPLETION_RULES;

import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.dms.DmsConfiguration.ChangeListener;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.MetadataAutocompletion;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.ftp.AlfrescoFtpService;
import org.cmdbuild.dms.alfresco.utils.XmlAutocompletionReader;
import org.cmdbuild.dms.alfresco.webservice.AlfrescoWsService;
import org.cmdbuild.dms.exception.DmsError;

import com.google.common.base.Supplier;

public class AlfrescoDmsService implements DmsService, LoggingSupport, ChangeListener {

	private final AlfrescoDmsConfiguration configuration;

	private Supplier<AlfrescoFtpService> ftpService;
	private Supplier<AlfrescoWsService> wsService;

	public AlfrescoDmsService(final AlfrescoDmsConfiguration configuration) {
		this.configuration = configuration;
		this.configuration.addListener(this);
		configurationChanged();
	}

	@Override
	public void configurationChanged() {
		ftpService = synchronizedSupplier(memoize(new Supplier<AlfrescoFtpService>() {

			@Override
			public AlfrescoFtpService get() {
				logger.info("initializing Alfresco inner services for ftp");
				return new AlfrescoFtpService(configuration);
			}

		}));
		wsService = synchronizedSupplier(memoize(new Supplier<AlfrescoWsService>() {

			@Override
			public AlfrescoWsService get() {
				logger.info("initializing Alfresco inner services for ws");
				return new AlfrescoWsService(configuration);
			}

		}));
	}

	@Override
	public Iterable<DocumentTypeDefinition> getTypeDefinitions() throws DmsError {
		return wsService.get().getDocumentTypeDefinitions();
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsError {
		ftpService.get().delete(document);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsError {
		return ftpService.get().download(document);
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
		return wsService.get().search(document);
	}

	@Override
	public void updateDescriptionAndMetadata(final DocumentUpdate document) throws DmsError {
		wsService.get().updateCategory(document);
		wsService.get().updateDescription(document);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsError {
		ftpService.get().upload(document);
		waitForSomeTimeBetweenFtpAndWebserviceOperations();
		try {
			wsService.get().updateCategory(document);
			wsService.get().updateProperties(document);
		} catch (final Exception e) {
			final String message = format("error updating metadata for file '%s' at path '%s'", //
					document.getFileName(), document.getPath());
			logger.error(message, e);
			ftpService.get().delete(documentDeleteFrom(document));
			throw DmsError.forward(e);
		}
	}

	/**
	 * This is very ugly! Old tests shows some problems if Webservice operations
	 * follows immediately FTP operations, so this delay was introduced.
	 */
	private void waitForSomeTimeBetweenFtpAndWebserviceOperations() {
		try {
			Thread.sleep(configuration.getDelayBetweenFtpAndWebserviceOperations());
		} catch (final InterruptedException e) {
			logger.warn("should never happen... so why?", e);
		}
	}

	private DocumentDelete documentDeleteFrom(final StorableDocument document) {
		return new DocumentDelete() {

			@Override
			public List<String> getPath() {
				return document.getPath();
			}

			@Override
			public String getClassName() {
				return document.getClassName();
			}

			@Override
			public Long getCardId() {
				return document.getCardId();
			}

			@Override
			public String getFileName() {
				return document.getFileName();
			}

		};
	}

	@Override
	public AutocompletionRules getAutoCompletionRules() throws DmsError {
		try {
			final String content = configuration.getMetadataAutocompletionFileContent();
			final AutocompletionRules autocompletionRules;
			if (isNotBlank(content)) {
				final MetadataAutocompletion.Reader reader = new XmlAutocompletionReader(content);
				autocompletionRules = reader.read();
			} else {
				autocompletionRules = NULL_AUTOCOMPLETION_RULES;
			}
			return autocompletionRules;
		} catch (final Exception e) {
			throw DmsError.forward(e);
		}
	}

	@Override
	public void clearCache() {
		final boolean isAlfrescoConfigured = wsService != null;
		if (isAlfrescoConfigured) {
			wsService.get().clearCache();
		}
	}

	@Override
	public void move(final StoredDocument document, final DocumentSearch from, final DocumentSearch to)
			throws DmsError {
		create(to);
		wsService.get().move(document, from, to);
	}

	@Override
	public void copy(final StoredDocument document, final DocumentSearch from, final DocumentSearch to)
			throws DmsError {
		create(to);
		wsService.get().copy(document, from, to);
	}

	@Override
	public void create(final DocumentSearch position) throws DmsError {
		ftpService.get().create(position);
	}

	@Override
	public void delete(final DocumentSearch position) throws DmsError {
		wsService.get().delete(position);
	}

	@Override
	public Map<String, String> getPresets() {
		return emptyMap();
	}

}
