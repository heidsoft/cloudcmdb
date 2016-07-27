package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.v1.constants.Serialization.CLASS_ID;
import static org.cmdbuild.service.rest.v1.constants.Serialization.ROLE_ID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.v1.model.ClassPrivilege;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;

@Path("roles/{" + ROLE_ID + "}/classes_privileges/")
@Produces(APPLICATION_JSON)
public interface ClassPrivileges {

	@GET
	@Path(EMPTY)
	ResponseMultiple<ClassPrivilege> read( //
			@PathParam(ROLE_ID) String roleId //
	);

	@GET
	@Path("{" + CLASS_ID + "}/")
	ResponseSingle<ClassPrivilege> read( //
			@PathParam(ROLE_ID) String roleId, //
			@PathParam(CLASS_ID) String classId //
	);

}
