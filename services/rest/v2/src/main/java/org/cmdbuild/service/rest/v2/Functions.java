package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FUNCTION_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PARAMETERS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.FunctionWithBasicDetails;
import org.cmdbuild.service.rest.v2.model.FunctionWithFullDetails;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Values;

@Path("functions/")
@Produces(APPLICATION_JSON)
public interface Functions {

	@GET
	@Path(EMPTY)
	ResponseMultiple<FunctionWithBasicDetails> readAll( //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset, //
			@QueryParam(FILTER) String filter //
	);

	@GET
	@Path("{" + FUNCTION_ID + "}/")
	ResponseSingle<FunctionWithFullDetails> read( //
			@PathParam(FUNCTION_ID) Long functionId //
	);

	@GET
	@Path("{" + FUNCTION_ID + "}/parameters/")
	ResponseMultiple<Attribute> readInputParameters( //
			@PathParam(FUNCTION_ID) Long functionId, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + FUNCTION_ID + "}/attributes/")
	ResponseMultiple<Attribute> readOutputParameters( //
			@PathParam(FUNCTION_ID) Long functionId, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + FUNCTION_ID + "}/outputs/")
	ResponseMultiple<Values> call( //
			@PathParam(FUNCTION_ID) Long functionId, //
			@QueryParam(PARAMETERS) String inputs //
	);

}
