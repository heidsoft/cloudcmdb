package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.v1.model.ProcessStatus;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;

@Path("configuration/processes/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface ProcessesConfiguration {

	@GET
	@Path("statuses/")
	ResponseMultiple<ProcessStatus> readStatuses();

}
