package org.cmdbuild.service.rest.v2.cxf;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.cmdbuild.service.rest.v2.model.Models.newFileSystemObject;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;

import java.util.Collection;
import java.util.Optional;

import javax.activation.DataHandler;

import org.cmdbuild.logic.files.Element;
import org.cmdbuild.logic.files.FileLogic;
import org.cmdbuild.logic.files.FileStore;
import org.cmdbuild.service.rest.v2.FileStores;
import org.cmdbuild.service.rest.v2.model.FileSystemObject;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;

public class CxfFileStores implements FileStores {

	private final ErrorHandler errorHandler;
	private final FileLogic fileLogic;

	public CxfFileStores(final ErrorHandler errorHandler, final FileLogic fileLogic) {
		this.errorHandler = requireNonNull(errorHandler, "missing " + ErrorHandler.class);
		this.fileLogic = requireNonNull(fileLogic, "missing " + FileLogic.class);
	}

	@Override
	public ResponseMultiple<FileSystemObject> readFolders(final String fileStoreId) {
		final FileStore store = assureFileStore(fileStoreId);
		final Collection<FileSystemObject> folders = stream(store.folders().spliterator(), false) //
				.map(input -> newFileSystemObject() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withParent(input.getParent()) //
						.build()) //
				.collect(toList());
		return newResponseMultiple(FileSystemObject.class) //
				.withElements(folders) //
				.withMetadata(newMetadata() //
						.withTotal(folders.size()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<FileSystemObject> readFolder(final String fileStoreId, final String folderId) {
		final FileStore store = assureFileStore(fileStoreId);
		final Optional<FileSystemObject> folder = store.folder(folderId) //
				.map(input -> newFileSystemObject() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withParent(input.getParent()) //
						.build());
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		return newResponseSingle(FileSystemObject.class) //
				.withElement(folder.get()) //
				.build();
	}

	@Override
	public ResponseSingle<FileSystemObject> uploadFile(final String fileStoreId, final String folderId,
			final DataHandler dataHandler) {
		final FileStore store = assureFileStore(fileStoreId);
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<Element> file = stream(store.files(folderId).spliterator(), false) //
				.filter(input -> input.getName().equals(dataHandler.getName())) //
				.findFirst();
		if (file.isPresent()) {
			errorHandler.duplicateFileName(dataHandler.getName());
		}
		final Optional<Element> created = store.create(folderId, dataHandler);
		if (!created.isPresent()) {
			errorHandler.fileNotCreated();
		}
		final Element element = created.get();
		return newResponseSingle(FileSystemObject.class) //
				.withElement(newFileSystemObject() //
						.withId(element.getId()) //
						.withName(element.getName()) //
						.withParent(element.getParent()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseMultiple<FileSystemObject> readFiles(final String fileStoreId, final String folderId) {
		final FileStore store = assureFileStore(fileStoreId);
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Collection<FileSystemObject> files = stream(store.files(folderId).spliterator(), false) //
				.map(input -> newFileSystemObject() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withParent(input.getParent()) //
						.build()) //
				.collect(toList());
		return newResponseMultiple(FileSystemObject.class) //
				.withElements(files) //
				.withMetadata(newMetadata() //
						.withTotal(files.size()) //
						.build()) //
				.build();
	}

	@Override
	public ResponseSingle<FileSystemObject> readFile(final String fileStoreId, final String folderId,
			final String fileId) {
		final FileStore store = assureFileStore(fileStoreId);
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<FileSystemObject> file = stream(store.files(folderId).spliterator(), false) //
				.filter(input -> input.getId().equals(fileId)) //
				.map(input -> newFileSystemObject() //
						.withId(input.getId()) //
						.withName(input.getName()) //
						.withParent(input.getParent()) //
						.build()) //
				.findFirst();
		if (!file.isPresent()) {
			errorHandler.fileNotFound(fileId);
		}
		return newResponseSingle(FileSystemObject.class) //
				.withElement(file.get()) //
				.build();
	}

	@Override
	public DataHandler downloadFile(final String fileStoreId, final String folderId, final String fileId) {
		final FileStore store = assureFileStore(fileStoreId);
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<Element> file = stream(store.files(folderId).spliterator(), false) //
				.filter(input -> input.getId().equals(fileId)) //
				.findFirst();
		if (!file.isPresent()) {
			errorHandler.fileNotFound(fileId);
		}
		return store.download(file.get()).get();
	}

	@Override
	public void deleteFile(final String fileStoreId, final String folderId, final String fileId) {
		final FileStore store = assureFileStore(fileStoreId);
		final Optional<Element> folder = store.folder(folderId);
		if (!folder.isPresent()) {
			errorHandler.folderNotFound(folderId);
		}
		final Optional<Element> file = stream(store.files(folderId).spliterator(), false) //
				.filter(input -> input.getId().equals(fileId)) //
				.findFirst();
		if (!file.isPresent()) {
			errorHandler.fileNotFound(fileId);
		}
		store.delete(file.get());
	}

	private FileStore assureFileStore(final String fileStoreId) {
		final FileStore output = fileLogic.fileStore(fileStoreId);
		if (output == null) {
			errorHandler.dataStoreNotFound(fileStoreId);
		}
		return output;
	}

}
