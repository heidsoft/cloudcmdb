package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PROCESS_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PROCESS_INSTANCE_ID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("processes/{" + PROCESS_ID + "}/instances/{" + PROCESS_INSTANCE_ID + "}/privileges/")
@Produces(APPLICATION_JSON)
public interface ProcessInstancePrivileges {

	@GET
	@Path(EMPTY)
	ResponseSingle<org.cmdbuild.service.rest.v2.model.ProcessInstancePrivileges> read( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId //
	);

}
