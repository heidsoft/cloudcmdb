package org.cmdbuild.servlets.json;

import static java.util.stream.StreamSupport.stream;
import static org.cmdbuild.services.json.dto.JsonResponse.failure;
import static org.cmdbuild.services.json.dto.JsonResponse.success;

import java.util.Optional;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logic.files.Element;
import org.cmdbuild.logic.files.FileStore;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.utils.FileItemDataSource;
import org.cmdbuild.servlets.utils.Parameter;

/**
 * @deprecated REST resource should be use instead.
 */
public class File extends JSONBaseWithSpringContext {

	/**
	 * @deprecated REST resource should be use instead.
	 */
	@JSONExported
	@Admin
	public JsonResponse upload( //
			@Parameter(value = "fileStore", required = true) final String fileStoreId,
			@Parameter(value = "folder", required = true) final String folderId,
			@Parameter(value = "file", required = true) final FileItem fileItem) {
		final JsonResponse response;
		do {
			final FileStore store = fileLogic().fileStore(fileStoreId);
			if (store == null) {
				response = failure("store not found", new IllegalArgumentException(fileStoreId));
				break;
			}
			final Optional<Element> folder = store.folder(folderId);
			if (!folder.isPresent()) {
				response = failure("folder not found", new IllegalArgumentException(folderId));
				break;
			}
			final Optional<Element> file = stream(store.files(folderId).spliterator(), false) //
					.filter(input -> input.getName().equals(fileItem.getName())) //
					.findFirst();
			if (file.isPresent()) {
				response = failure("duplicate file name", new IllegalArgumentException(fileItem.getName()));
				break;
			}
			final Optional<Element> created = store.create(folderId, new DataHandler(FileItemDataSource.of(fileItem)));
			if (!created.isPresent()) {
				response = failure("file not created", new IllegalArgumentException(fileItem.getName()));
				break;
			}
			response = success(created.get().getId());
		} while (false);
		return response;
	}

}
