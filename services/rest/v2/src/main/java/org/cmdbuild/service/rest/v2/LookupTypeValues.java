package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LOOKUP_TYPE_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LOOKUP_VALUE_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.LookupDetail;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("lookup_types/{" + LOOKUP_TYPE_ID + "}/values/")
@Produces(APPLICATION_JSON)
public interface LookupTypeValues {

	@GET
	@Path("{" + LOOKUP_VALUE_ID + "}/")
	ResponseSingle<LookupDetail> read( //
			@PathParam(LOOKUP_TYPE_ID) String lookupTypeId, //
			@PathParam(LOOKUP_VALUE_ID) Long lookupValueId //
	);

	@GET
	@Path(EMPTY)
	ResponseMultiple<LookupDetail> readAll( //
			@PathParam(LOOKUP_TYPE_ID) String lookupTypeId, //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

}
