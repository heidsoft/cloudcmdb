package org.cmdbuild.logic.files;

import static java.util.stream.StreamSupport.stream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.activation.DataHandler;

import org.cmdbuild.services.FilesStore;

public class DefaultFileSystemFacade implements FileSystemFacade {

	private final FilesStore filesStore;

	public DefaultFileSystemFacade(final FilesStore filesStore) {
		this.filesStore = filesStore;
	}

	@Override
	public File root() {
		return filesStore.getRoot();
	}

	@Override
	public Collection<File> directories() {
		final Collection<File> output = new ArrayList<>();
		directories(filesStore, output);
		return output;
	}

	private static void directories(final FilesStore filesStore, final Collection<File> output) {
		output.add(filesStore.getRoot());
		stream(filesStore.files(null).spliterator(), false) //
				.filter(File::isDirectory) //
				.forEach(input -> directories(filesStore.sub(input.getName()), output));
	}

	@Override
	public File save(final DataHandler dataHandler, final String path) throws IOException {
		return filesStore.save(dataHandler.getInputStream(), path);
	}

}
