package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DOMAIN_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v1.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v1.constants.Serialization.START;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v1.model.DomainWithBasicDetails;
import org.cmdbuild.service.rest.v1.model.DomainWithFullDetails;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

@Path("domains/")
@Produces(APPLICATION_JSON)
public interface Domains {

	@GET
	@Path(EMPTY)
	ResponseMultiple<DomainWithBasicDetails> readAll( //
			@QueryParam(FILTER) String filter, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + DOMAIN_ID + "}/")
	ResponseSingle<DomainWithFullDetails> read( //
			@PathParam(DOMAIN_ID) String domainId //
	);

}
