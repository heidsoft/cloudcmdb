package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.ATTACHMENT_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.PROCESS_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.PROCESS_INSTANCE_ID;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.v1.model.Attachment;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

@Path("processes/{" + PROCESS_ID + "}/instances/{" + PROCESS_INSTANCE_ID + "}/attachments/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface ProcessInstanceAttachments {

	/*
	 * POST method must be implemented within specific JAX-RS implementation
	 */

	@GET
	@Path(EMPTY)
	ResponseMultiple<Attachment> read( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId //
	);

	@GET
	@Path("{" + ATTACHMENT_ID + "}/")
	ResponseSingle<Attachment> read( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

	@GET
	@Path("{" + ATTACHMENT_ID + "}/{file: [^/]+}")
	@Produces(APPLICATION_OCTET_STREAM)
	DataHandler download( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

	/*
	 * PUT method must be implemented within specific JAX-RS implementation
	 */

	@DELETE
	@Path("{" + ATTACHMENT_ID + "}/")
	void delete( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

}