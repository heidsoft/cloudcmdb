package org.cmdbuild.service.rest.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.cmdbuild.service.rest.v2.constants.Serialization.FILE;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.service.rest.v2.model.FileSystemObject;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

@Path("filestores/")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface FileStores {

	String DATASTORE_ID = "datastoreId";
	String FOLDER_ID = "folderId";
	String FILE_ID = "fileId";

	@GET
	@Path("{" + DATASTORE_ID + "}/folders/")
	ResponseMultiple<FileSystemObject> readFolders( //
			@PathParam(DATASTORE_ID) String datastoreId //
	);

	@GET
	@Path("{" + DATASTORE_ID + "}/folders/{" + FOLDER_ID + "}/")
	ResponseSingle<FileSystemObject> readFolder( //
			@PathParam(DATASTORE_ID) String datastoreId, //
			@PathParam(FOLDER_ID) String folderId //
	);

	@POST
	@Path("{" + DATASTORE_ID + "}/folders/{" + FOLDER_ID + "}/files/")
	@Consumes(MULTIPART_FORM_DATA)
	ResponseSingle<FileSystemObject> uploadFile( //
			@PathParam(DATASTORE_ID) String datastoreId, //
			@PathParam(FOLDER_ID) String folderId, //
			@Multipart(FILE) DataHandler dataHandler //
	);

	@GET
	@Path("{" + DATASTORE_ID + "}/folders/{" + FOLDER_ID + "}/files/")
	ResponseMultiple<FileSystemObject> readFiles( //
			@PathParam(DATASTORE_ID) String datastoreId, //
			@PathParam(FOLDER_ID) String folderId //
	);

	@GET
	@Path("{" + DATASTORE_ID + "}/folders/{" + FOLDER_ID + "}/files/{" + FILE_ID + "}/")
	ResponseSingle<FileSystemObject> readFile( //
			@PathParam(DATASTORE_ID) String datastoreId, //
			@PathParam(FOLDER_ID) String folderId, //
			@PathParam(FILE_ID) String fileId //
	);

	@GET
	@Path("{" + DATASTORE_ID + "}/folders/{" + FOLDER_ID + "}/files/{" + FILE_ID + "}/download")
	@Produces(APPLICATION_OCTET_STREAM)
	DataHandler downloadFile( //
			@PathParam(DATASTORE_ID) String datastoreId, //
			@PathParam(FOLDER_ID) String folderId, //
			@PathParam(FILE_ID) String fileId //
	);

	@DELETE
	@Path("{" + DATASTORE_ID + "}/folders/{" + FOLDER_ID + "}/files/{" + FILE_ID + "}/")
	void deleteFile( //
			@PathParam(DATASTORE_ID) String datastoreId, //
			@PathParam(FOLDER_ID) String folderId, //
			@PathParam(FILE_ID) String fileId //
	);

}
