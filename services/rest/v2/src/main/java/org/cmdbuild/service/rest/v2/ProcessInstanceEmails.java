package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.EMAIL_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PROCESS_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PROCESS_INSTANCE_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.Email;
import org.cmdbuild.service.rest.v2.model.LongId;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("processes/{" + PROCESS_ID + "}/instances/{" + PROCESS_INSTANCE_ID + "}/emails/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface ProcessInstanceEmails {

	@GET
	@Path("statuses/")
	ResponseMultiple<String> statuses();

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			Email email //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<LongId> readAll( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + EMAIL_ID + "}/")
	ResponseSingle<Email> read( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			@PathParam(EMAIL_ID) Long emailId //
	);

	@PUT
	@Path("{" + EMAIL_ID + "}/")
	void update( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			@PathParam(EMAIL_ID) Long emailId, //
			Email email //
	);

	@DELETE
	@Path("{" + EMAIL_ID + "}/")
	void delete( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			@PathParam(EMAIL_ID) Long emailId //
	);

}
