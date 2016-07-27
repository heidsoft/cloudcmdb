package org.cmdbuild.service.rest.v2.cxf;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.cmdbuild.service.rest.v2.logging.LoggingSupport;

public class WebApplicationExceptionErrorHandler implements ErrorHandler, LoggingSupport {

	@Override
	public void alreadyExistingAttachmentName(final String value) {
		logger.error("already existing attachment '{}'", value);
		badRequest(format("already existing attachment '%s'", value));
	}

	@Override
	public void attachmentNotFound(final String value) {
		logger.error("attachment not found '{}'", value);
		notFound(value);
	}

	@Override
	public void cardNotFound(final Long value) {
		logger.error("card not found '{}'", value);
		notFound(value);
	}

	@Override
	public void classNotFound(final String value) {
		logger.error("class not found '{}'", value);
		notFound(value);
	}

	@Override
	public void classNotFoundClassIsProcess(final String value) {
		logger.error("class '{}' is a process", value);
		notFound(value);
	}

	@Override
	public void dataStoreNotFound(final String value) {
		logger.error("data store '{}' not found", value);
		notFound(value);
	}

	@Override
	public void differentAttachmentName(final String value) {
		logger.error("different file value '{}'", value);
		badRequest(format("different file value '%s'", value));
	}

	@Override
	public void domainNotFound(final String value) {
		logger.error("domain not found '{}'", value);
		notFound(value);
	}

	@Override
	public void domainTreeNotFound(final String value) {
		logger.error("domain tree found '{}'", value);
		notFound(value);
	}

	@Override
	public void duplicateFileName(final String value) {
		logger.error("duplicate file name '{}'", value);
		conflict(value);
	}

	@Override
	public void extensionNotFound(final String value) {
		logger.error("extension not found '{}'", value);
		notFound(value);
	}

	@Override
	public void fileNotCreated() {
		logger.error("file not created");
		generic("file not created");
	}

	@Override
	public void fileNotFound(final String value) {
		logger.error("file not found '{}'", value);
		notFound(value);
	}

	@Override
	public void folderNotFound(final String value) {
		logger.error("folder not found '{}'", value);
		notFound(value);
	}

	@Override
	public void functionNotFound(final Long value) {
		logger.error("function not found '{}'", value);
		notFound(value);
	}

	@Override
	public void invalidIconType(final String type) {
		logger.error("invalid icon type '{}'", type);
		badRequest(type);
	}

	@Override
	public void invalidType(final String value) {
		logger.error("invalid param '{}'", value);
		badRequest(value);
	}

	@Override
	public void lookupTypeNotFound(final String value) {
		logger.error("lookup type not found '{}'", value);
		notFound(value);
	}

	@Override
	public void missingAttachmentId() {
		logger.error("missing attachment's value");
		notFound("attachment's value");
	}

	@Override
	public void missingAttachmentMetadata() {
		logger.error("missing attachment's metadata");
		notFound("attachment's metadata");
	}

	@Override
	public void missingAttachmentName() {
		logger.error("missing attachment's value");
		notFound("attachment's value");
	}

	@Override
	public void missingFile() {
		logger.error("missing file");
		notFound("attachment's file");
	}

	@Override
	public void missingIcon(final Long value) {
		logger.error("missing icon '{}'", value);
		notFound(value);
	}

	@Override
	public void missingParam(final String value) {
		logger.error("missing param '{}'", value);
		notFound(value);
	}

	@Override
	public void missingPassword() {
		logger.error("missing password");
		badRequest("missing password");
	}

	@Override
	public void missingUsername() {
		logger.error("missing username");
		badRequest("missing username");
	}

	@Override
	public void notAuthorized() {
		logger.error("not authorized");
		throw new WebApplicationException(Response.status(UNAUTHORIZED) //
				.build());
	}

	@Override
	public void processActivityNotFound(final String value) {
		logger.error("process instance activity not found '{}'", value);
		notFound(value);
	}

	@Override
	public void processInstanceNotFound(final Long value) {
		logger.error("process instance not found '{}'", value);
		notFound(value);
	}

	@Override
	public void processNotFound(final String value) {
		logger.error("process not found '{}'", value);
		notFound(value);
	}

	@Override
	public void propagate(final Throwable e) {
		logger.error("unhandled exception", e);
		throw new WebApplicationException(e,
				Response.serverError() //
						.entity(e) //
						.build());
	}

	@Override
	public void relationNotFound(final Long value) {
		logger.error("relation not found '{}'", value);
		notFound(value);
	}

	@Override
	public void reportNotFound(final Long value) {
		logger.error("report not found '{}'", value);
		notFound(value);
	}

	@Override
	public void roleNotFound(final String value) {
		logger.error("role not found '{}'", value);
		notFound(value);
	}

	@Override
	public void sessionNotFound(final String value) {
		logger.error("session not found '{}'", value);
		notFound(value);
	}

	private void badRequest(final Object entity) {
		throw new WebApplicationException(Response.status(BAD_REQUEST) //
				.entity(entity) //
				.build());
	}

	private void conflict(final Object entity) {
		throw new WebApplicationException(Response.status(CONFLICT) //
				.entity(entity) //
				.build());
	}

	private void generic(final Object entity) {
		throw new WebApplicationException(Response.status(INTERNAL_SERVER_ERROR) //
				.entity(entity) //
				.build());
	}

	private void notFound(final Object entity) {
		throw new WebApplicationException(Response.status(NOT_FOUND) //
				.entity(entity) //
				.build());
	}

}
