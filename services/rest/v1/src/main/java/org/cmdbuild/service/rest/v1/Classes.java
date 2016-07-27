package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.v1.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v1.model.ClassWithBasicDetails;
import org.cmdbuild.service.rest.v1.model.ClassWithFullDetails;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

@Path("classes/")
@Produces(APPLICATION_JSON)
public interface Classes {

	@GET
	@Path(EMPTY)
	ResponseMultiple<ClassWithBasicDetails> readAll( //
			@QueryParam(ACTIVE) boolean activeOnly, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + CLASS_ID + "}/")
	ResponseSingle<ClassWithFullDetails> read( //
			@PathParam(CLASS_ID) String classId //
	);

}
