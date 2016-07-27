package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.LOOKUP_TYPE_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v1.model.LookupTypeDetail;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

@Path("lookup_types/")
@Produces(APPLICATION_JSON)
public interface LookupTypes {

	@GET
	@Path("{" + LOOKUP_TYPE_ID + "}/")
	ResponseSingle<LookupTypeDetail> read( //
			@PathParam(LOOKUP_TYPE_ID) String lookupTypeId //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<LookupTypeDetail> readAll( //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}
