package org.cmdbuild.service.rest.v1.cxf;

import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.cmdbuild.service.rest.v1.logging.LoggingSupport;

public class WebApplicationExceptionErrorHandler implements ErrorHandler, LoggingSupport {

	@Override
	public void alreadyExistingAttachmentName(final String name) {
		logger.error("already existing attachment '{}'", name);
		badRequest(format("already existing attachment '%s'", name));
	}

	@Override
	public void attachmentNotFound(final String id) {
		logger.error("attachment not found '{}'", id);
		notFound(id);
	}

	@Override
	public void cardNotFound(final Long id) {
		logger.error("card not found '{}'", id);
		notFound(id);
	}

	@Override
	public void classNotFound(final String id) {
		logger.error("class not found '{}'", id);
		notFound(id);
	}

	@Override
	public void classNotFoundClassIsProcess(final String id) {
		logger.error("class '{}' is a process", id);
		notFound(id);
	}

	@Override
	public void differentAttachmentName(final String name) {
		logger.error("different file name '{}'", name);
		badRequest(format("different file name '%s'", name));
	}

	@Override
	public void domainNotFound(final String id) {
		logger.error("domain not found '{}'", id);
		notFound(id);
	}

	@Override
	public void missingUsername() {
		logger.error("missing username");
		throw new WebApplicationException(Response.status(BAD_REQUEST) //
				.entity("missing username") //
				.build());
	}

	@Override
	public void invalidType(final String id) {
		logger.error("invalid param '{}'", id);
		throw new WebApplicationException(Response.status(BAD_REQUEST) //
				.entity(id) //
				.build());
	}

	@Override
	public void lookupTypeNotFound(final String id) {
		logger.error("lookup type not found '{}'", id);
		notFound(id);
	}

	@Override
	public void missingAttachmentId() {
		logger.error("missing attachment's id");
		notFound("attachment's id");
	}

	@Override
	public void missingAttachmentName() {
		logger.error("missing attachment's name");
		notFound("attachment's name");
	}

	@Override
	public void missingAttachmentMetadata() {
		logger.error("missing attachment's metadata");
		notFound("attachment's metadata");
	}

	@Override
	public void missingFile() {
		logger.error("missing file");
		notFound("attachment's file");
	}

	@Override
	public void missingParam(final String name) {
		logger.error("missing param '{}'", name);
		notFound(name);
	}

	@Override
	public void missingPassword() {
		logger.error("missing password");
		throw new WebApplicationException(Response.status(BAD_REQUEST) //
				.entity("missing password") //
				.build());
	}

	@Override
	public void notAuthorized() {
		logger.error("not authorized");
		throw new WebApplicationException(Response.status(UNAUTHORIZED) //
				.build());
	}

	@Override
	public void processNotFound(final String id) {
		logger.error("process not found '{}'", id);
		notFound(id);
	}

	@Override
	public void processInstanceNotFound(final Long id) {
		logger.error("process instance not found '{}'", id);
		notFound(id);
	}

	@Override
	public void processActivityNotFound(final String id) {
		logger.error("process instance activity not found '{}'", id);
		notFound(id);
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
	public void relationNotFound(final Long id) {
		logger.error("relation not found '{}'", id);
		notFound(id);
	}

	@Override
	public void roleNotFound(final String id) {
		logger.error("role not found '{}'", id);
		notFound(id);
	}

	@Override
	public void sessionNotFound(final String id) {
		logger.error("session not found '{}'", id);
		notFound(id);
	}

	private void badRequest(final Object entity) {
		throw new WebApplicationException(Response.status(BAD_REQUEST) //
				.entity(entity) //
				.build());
	}

	private void notFound(final Object entity) {
		throw new WebApplicationException(Response.status(NOT_FOUND) //
				.entity(entity) //
				.build());
	}

}
