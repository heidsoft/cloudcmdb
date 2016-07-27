package org.cmdbuild.dms;

import java.io.InputStream;

public interface DocumentCreator {

	DocumentSearch createDocumentSearch( //
			String className, //
			Long cardId);

	StorableDocument createStorableDocument( //
			String author, //
			String className, //
			Long cardId, //
			InputStream inputStream, //
			String fileName, //
			String category, //
			String description);

	StorableDocument createStorableDocument( //
			String author, //
			String className, //
			Long cardId, //
			InputStream inputStream, //
			String fileName, //
			String category, //
			String description, //
			Iterable<MetadataGroup> metadataGroups);

	DocumentDownload createDocumentDownload( //
			String className, //
			Long cardId, //
			String fileName);

	DocumentDelete createDocumentDelete( //
			String className, //
			Long cardId, //
			String fileName);

	DocumentUpdate createDocumentUpdate( //
			String className, //
			Long cardId, //
			String filename, //
			String category, //
			String description, //
			String author);

	DocumentUpdate createDocumentUpdate( //
			String className, //
			Long cardId, //
			String filename, //
			String category, //
			String description, //
			String author, //
			Iterable<MetadataGroup> metadataGroups);

}
