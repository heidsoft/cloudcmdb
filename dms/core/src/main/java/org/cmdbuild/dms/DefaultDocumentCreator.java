package org.cmdbuild.dms;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;

public class DefaultDocumentCreator implements DocumentCreator {

	private static final Collection<MetadataGroup> lol = emptyList();

	private final Iterable<String> basePath;

	public DefaultDocumentCreator(final Iterable<String> basePath) {
		Validate.notNull(basePath, "null path");
		this.basePath = basePath;
	}

	@Override
	public DocumentSearch createDocumentSearch(final String className, final Long cardId) {
		return new DocumentSearch() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public Long getCardId() {
				return cardId;
			}

			@Override
			public List<String> getPath() {
				return path(cardId);
			}

		};

	}

	@Override
	public StorableDocument createStorableDocument(final String author, final String className, final Long cardId,
			final InputStream inputStream, final String fileName, final String category, final String description) {
		return createStorableDocument(author, className, cardId, inputStream, fileName, category, description, lol);
	}

	@Override
	public StorableDocument createStorableDocument(final String author, final String className, final Long cardId,
			final InputStream inputStream, final String fileName, final String category, final String description,
			final Iterable<MetadataGroup> metadataGroups) {
		return new StorableDocument() {

			@Override
			public String getAuthor() {
				return author;
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
			public List<String> getPath() {
				return path(cardId);
			}

			@Override
			public InputStream getInputStream() {
				return inputStream;
			}

			@Override
			public String getFileName() {
				return fileName;
			}

			@Override
			public String getCategory() {
				return category;
			}

			@Override
			public String getDescription() {
				return description;
			}

			@Override
			public Iterable<MetadataGroup> getMetadataGroups() {
				return metadataGroups;
			}

		};
	}

	@Override
	public DocumentDownload createDocumentDownload(final String className, final Long cardId, final String fileName) {
		return new DocumentDownload() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public Long getCardId() {
				return cardId;
			}

			@Override
			public List<String> getPath() {
				return path(cardId);
			}

			@Override
			public String getFileName() {
				return fileName;
			}

		};
	}

	@Override
	public DocumentDelete createDocumentDelete(final String className, final Long cardId, final String fileName) {
		return new DocumentDelete() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public Long getCardId() {
				return cardId;
			}

			@Override
			public List<String> getPath() {
				return path(cardId);
			}

			@Override
			public String getFileName() {
				return fileName;
			}

		};
	}

	@Override
	public DocumentUpdate createDocumentUpdate(final String className, final Long cardId, final String filename,
			final String category, final String description, final String author) {
		return createDocumentUpdate(className, cardId, filename, category, description, author, lol);
	}

	@Override
	public DocumentUpdate createDocumentUpdate(final String className, final Long cardId, final String filename,
			final String category, final String description, final String author,
			final Iterable<MetadataGroup> metadataGroups) {
		return new DocumentUpdate() {

			@Override
			public String getClassName() {
				return className;
			}

			@Override
			public Long getCardId() {
				return cardId;
			}

			@Override
			public List<String> getPath() {
				return path(cardId);
			}

			@Override
			public String getFileName() {
				return filename;
			}

			@Override
			public String getCategory() {
				return category;
			}

			@Override
			public String getDescription() {
				return description;
			}

			@Override
			public String getAuthor() {
				return author;
			}

			@Override
			public Iterable<MetadataGroup> getMetadataGroups() {
				return metadataGroups;
			}

		};
	}

	private List<String> path(final Long cardId) {
		final List<String> fullPath = Lists.newArrayList(basePath);
		if (cardId != null) {
			fullPath.add("Id" + cardId);
		}
		return unmodifiableList(fullPath);
	}

}
