package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ATTACHMENT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ATTACHMENT_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILE;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.service.rest.v2.model.Attachment;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("classes/{" + CLASS_ID + "}/cards/{" + CARD_ID + "}/attachments/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface CardAttachments {

	@POST
	@Path(EMPTY)
	@Consumes(MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	ResponseSingle<String> create( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@Multipart(value = ATTACHMENT, required = false) Attachment attachment, //
			@Multipart(FILE) DataHandler dataHandler //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Attachment> read( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId //
	);

	@GET
	@Path("{" + ATTACHMENT_ID + "}/")
	ResponseSingle<Attachment> read( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

	@GET
	@Path("{" + ATTACHMENT_ID + "}/{file: [^/]+}")
	@Produces(APPLICATION_OCTET_STREAM)
	DataHandler download( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

	@PUT
	@Path("{" + ATTACHMENT_ID + "}/")
	@Consumes(MULTIPART_FORM_DATA)
	@Produces(APPLICATION_JSON)
	void update( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId, //
			@Multipart(value = ATTACHMENT, required = false) Attachment attachment, //
			@Multipart(value = FILE, required = false) DataHandler dataHandler //
	);

	@DELETE
	@Path("{" + ATTACHMENT_ID + "}/")
	void delete( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long cardId, //
			@PathParam(ATTACHMENT_ID) String attachmentId //
	);

}