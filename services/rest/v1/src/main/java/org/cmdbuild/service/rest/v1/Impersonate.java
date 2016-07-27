package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.SESSION;
import static org.cmdbuild.service.rest.v1.constants.Serialization.USERNAME;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("sessions/{" + SESSION + "}/impersonate/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Impersonate {

	@PUT
	@Path("{" + USERNAME + "}/")
	void start( //
			@PathParam(SESSION) String id, //
			@PathParam(USERNAME) String username //
	);

	@DELETE
	@Path(EMPTY)
	void stop( //
			@PathParam(SESSION) String id //
	);

}
