package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.SORT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.Card;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;

@Path("cql/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface Cql {

	@GET
	@Path(EMPTY)
	ResponseMultiple<Card> read( //
			@QueryParam(FILTER) String filter, //
			@QueryParam(SORT) String sort, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}
