package org.cmdbuild.dms;

public interface DocumentUpdate extends Document, DocumentWithMetadata {

	String getFileName();

	String getCategory();

	String getDescription();

	String getAuthor();

}
