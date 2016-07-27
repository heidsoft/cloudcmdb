package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PROCESS_ACTIVITY_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PROCESS_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PROCESS_INSTANCE_ID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.v2.model.ProcessActivityWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.ProcessActivityWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("processes/{" + PROCESS_ID + "}/instances/{" + PROCESS_INSTANCE_ID + "}/activities/")
@Produces(APPLICATION_JSON)
public interface ProcessInstanceActivities {

	@GET
	@Path(EMPTY)
	ResponseMultiple<ProcessActivityWithBasicDetails> read( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId //
	);

	@GET
	@Path("{" + PROCESS_ACTIVITY_ID + "}/")
	ResponseSingle<ProcessActivityWithFullDetails> read( //
			@PathParam(PROCESS_ID) String processId, //
			@PathParam(PROCESS_INSTANCE_ID) Long processInstanceId, //
			@PathParam(PROCESS_ACTIVITY_ID) String processActivityId //
	);

}
