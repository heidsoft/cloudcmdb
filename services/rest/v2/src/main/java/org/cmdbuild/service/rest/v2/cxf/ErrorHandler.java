package org.cmdbuild.service.rest.v2.cxf;

public interface ErrorHandler {

	void alreadyExistingAttachmentName(String value);

	void attachmentNotFound(String value);

	void cardNotFound(Long value);

	void classNotFound(String value);

	void classNotFoundClassIsProcess(String value);

	void dataStoreNotFound(String value);

	void differentAttachmentName(String value);

	void domainNotFound(String value);

	void domainTreeNotFound(String value);

	void duplicateFileName(String value);

	void extensionNotFound(String value);

	void fileNotCreated();

	void fileNotFound(String value);

	void folderNotFound(String value);

	void functionNotFound(Long value);

	void invalidIconType(String type);

	void invalidType(String value);

	void lookupTypeNotFound(String value);

	void missingAttachmentId();

	void missingAttachmentMetadata();

	void missingAttachmentName();

	void missingFile();

	void missingIcon(Long value);

	void missingParam(String value);

	void missingPassword();

	void missingUsername();

	void notAuthorized();

	void processActivityNotFound(String value);

	void processInstanceNotFound(Long value);

	void processNotFound(String value);

	void propagate(Throwable e);

	void relationNotFound(Long value);

	void reportNotFound(Long value);

	void roleNotFound(String value);

	void sessionNotFound(String value);

}