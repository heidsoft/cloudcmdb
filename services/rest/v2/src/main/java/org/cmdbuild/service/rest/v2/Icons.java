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

import org.cmdbuild.service.rest.v2.model.Icon;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("icons/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Icons {

	@POST
	@Path(EMPTY)
	ResponseSingle<Icon> create( //
			Icon icon //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Icon> read( //
	);

	@GET
	@Path("{" + ID + "}/")
	ResponseSingle<Icon> read( //
			@PathParam(ID) Long id //
	);

	@PUT
	@Path("{" + ID + "}/")
	void update( //
			@PathParam(ID) Long id, //
			Icon icon //
	);

	@DELETE
	@Path("{" + ID + "}/")
	void delete( //
			@PathParam(ID) Long id //
	);

}
