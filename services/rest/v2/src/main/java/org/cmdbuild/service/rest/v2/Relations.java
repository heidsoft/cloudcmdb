package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DETAILED;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DOMAIN_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.RELATION_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.Relation;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("domains/{" + DOMAIN_ID + "}/relations/")
@Produces(APPLICATION_JSON)
public interface Relations {

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			@PathParam(DOMAIN_ID) String classId, //
			Relation relation //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Relation> read( //
			@PathParam(DOMAIN_ID) String domainId, //
			@QueryParam(FILTER) String filter, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset, //
			@QueryParam(DETAILED) boolean detailed //
	);

	@GET
	@Path("{" + RELATION_ID + "}/")
	ResponseSingle<Relation> read( //
			@PathParam(DOMAIN_ID) String domainId, //
			@PathParam(RELATION_ID) Long relationId //
	);

	@PUT
	@Path("{" + RELATION_ID + "}/")
	void update( //
			@PathParam(DOMAIN_ID) String domainId, //
			@PathParam(RELATION_ID) Long relationId, //
			Relation relation //
	);

	@DELETE
	@Path("{" + RELATION_ID + "}/")
	void delete( //
			@PathParam(DOMAIN_ID) String domainId, //
			@PathParam(RELATION_ID) Long relationId //
	);

}
