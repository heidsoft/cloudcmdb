package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Session;

@Path("sessions/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Sessions {

	@POST
	@Path(EMPTY)
	@Unauthorized
	ResponseSingle<Session> create( //
			Session session //
	);

	@GET
	@Path("{" + ID + "}/")
	ResponseSingle<Session> read( //
			@PathParam(ID) String id //
	);

	@PUT
	@Path("{" + ID + "}/")
	ResponseSingle<Session> update( //
			@PathParam(ID) String id, //
			Session session //
	);

	@DELETE
	@Path("{" + ID + "}/")
	void delete( //
			@PathParam(ID) String id //
	);

}
