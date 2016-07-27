package org.cmdbuild.service.rest.v1.cxf;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.ATTACHMENT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.ATTACHMENT_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.FILE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.PROCESS_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.PROCESS_INSTANCE_ID;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.service.rest.v1.model.Attachment;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

@Path("processes/{" + PROCESS_ID + "}/instances/{" + PROCESS_INSTANCE_ID + "}/attachments/")
public interface ProcessInstanceAttachments {

	@POST
	@Path(EMPTY)
	@Consumes(MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	ResponseSingle<String> create( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long instanceId, //
			@Multipart(value = ATTACHMENT, required = false) Attachment attachment, //
			@Multipart(FILE) DataHandler dataHandler //
	);

	@PUT
	@Path("{" + ATTACHMENT_ID + "}/")
	@Consumes(MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	void update( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long instanceId, //
			@PathParam(ATTACHMENT_ID) String attachmentId, //
			@Multipart(value = ATTACHMENT, required = false) Attachment attachment, //
			@Multipart(value = FILE, required = false) DataHandler dataHandler //
	);

}