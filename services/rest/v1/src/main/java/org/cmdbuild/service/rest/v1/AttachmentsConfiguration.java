package org.cmdbuild.service.rest.v1;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.v1.constants.Serialization.CATEGORY_ID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.cmdbuild.service.rest.v1.model.AttachmentCategory;
import org.cmdbuild.service.rest.v1.model.Attribute;
import org.cmdbuild.service.rest.v1.model.ResponseMultiple;

@Path("configuration/attachments/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface AttachmentsConfiguration {

	@GET
	@Path("categories/")
	ResponseMultiple<AttachmentCategory> readCategories();

	@GET
	@Path("categories/{" + CATEGORY_ID + "}/attributes/")
	ResponseMultiple<Attribute> readCategoryAttributes( //
			@PathParam(CATEGORY_ID) String categoryId //
	);

}
