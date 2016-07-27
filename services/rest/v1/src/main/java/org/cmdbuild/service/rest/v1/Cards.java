package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.CARD_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v1.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.POSITION_OF;
import static org.cmdbuild.service.rest.v1.constants.Serialization.SORT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.START;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v1.model.Card;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

@Path("classes/{" + CLASS_ID + "}/cards/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Cards {

	@POST
	@Path(EMPTY)
	ResponseSingle<Long> create( //
			@PathParam(CLASS_ID) String classId, //
			Card card //
	);

	@GET
	@Path("{" + CARD_ID + "}/")
	ResponseSingle<Card> read( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long id //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<Card> read( //
			@PathParam(CLASS_ID) String classId, //
			@QueryParam(FILTER) String filter, //
			@QueryParam(SORT) String sort, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset, //
			@QueryParam(POSITION_OF) Set<Long> cardIds //
	);

	@PUT
	@Path("{" + CARD_ID + "}/")
	void update( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long id, //
			Card card //
	);

	@DELETE
	@Path("{" + CARD_ID + "}/")
	void delete( //
			@PathParam(CLASS_ID) String classId, //
			@PathParam(CARD_ID) Long id //
	);

}
