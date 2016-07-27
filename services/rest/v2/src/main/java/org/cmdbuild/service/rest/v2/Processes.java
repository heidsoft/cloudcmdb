package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PROCESS_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.ProcessWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.ProcessWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("processes/")
@Produces(APPLICATION_JSON)
public interface Processes {

	@GET
	@Path(EMPTY)
	ResponseMultiple<ProcessWithBasicDetails> readAll( //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + PROCESS_ID + "}/")
	ResponseSingle<ProcessWithFullDetails> read( //
			@PathParam(PROCESS_ID) String processId //
	);

	@GET
	@Path("{" + PROCESS_ID + "}/generate_id")
	ResponseSingle<Long> generateId( //
			@PathParam(PROCESS_ID) String processId //
	);

}
