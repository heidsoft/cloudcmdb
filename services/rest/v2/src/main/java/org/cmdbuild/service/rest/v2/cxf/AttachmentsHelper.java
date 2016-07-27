package org.cmdbuild.service.rest.v2.cxf;

import javax.activation.DataHandler;

import org.cmdbuild.service.rest.v2.model.Attachment;

import com.google.common.base.Optional;

public interface AttachmentsHelper {

	String create(String classId, Long cardId, String attachmentName, Attachment attachment, DataHandler dataHandler)
			throws Exception;

	void update(String classId, Long cardId, String attachmentId, Attachment attachment, DataHandler dataHandler)
			throws Exception;

	Iterable<Attachment> search(String classId, Long cardId);

	Optional<Attachment> search(String classId, Long cardId, String attachmentId);

	DataHandler download(String classId, Long cardId, String attachmentId);

	void delete(String classId, Long cardId, String attachmentId);

}