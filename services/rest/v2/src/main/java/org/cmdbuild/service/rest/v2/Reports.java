package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v2.constants.Serialization.EXTENSION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.v2.constants.Serialization.LIMIT;
import static org.cmdbuild.service.rest.v2.constants.Serialization.PARAMETERS;
import static org.cmdbuild.service.rest.v2.constants.Serialization.REPORT_ID;
import static org.cmdbuild.service.rest.v2.constants.Serialization.START;

import javax.activation.DataHandler;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.cmdbuild.service.rest.v2.model.Attribute;
import org.cmdbuild.service.rest.v2.model.JsonValues;
import org.cmdbuild.service.rest.v2.model.LongIdAndDescription;
import org.cmdbuild.service.rest.v2.model.Report;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("reports/")
@Produces(APPLICATION_JSON)
public interface Reports {

	@GET
	@Path(EMPTY)
	ResponseMultiple<LongIdAndDescription> readAll( //
			@QueryParam(FILTER) String filter, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + REPORT_ID + "}/")
	ResponseSingle<Report> read( //
			@PathParam(REPORT_ID) Long reportId //
	);

	@GET
	@Path("{" + REPORT_ID + "}/attributes/")
	ResponseMultiple<Attribute> readAllAttributes( //
			@PathParam(REPORT_ID) Long reportId, //
			@QueryParam(LIMIT) Integer limit, //
			@QueryParam(START) Integer offset //
	);

	@GET
	@Path("{" + REPORT_ID + "}/{file: [^/]+}")
	@Produces(APPLICATION_OCTET_STREAM)
	DataHandler download( //
			@PathParam(REPORT_ID) Long reportId, //
			@QueryParam(EXTENSION) String extension, //
			@QueryParam(PARAMETERS) JsonValues parameters //
	);

}
