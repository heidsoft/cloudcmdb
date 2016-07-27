package org.cmdbuild.logic.files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.activation.DataHandler;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingFileSystemFacade extends ForwardingObject implements FileSystemFacade {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingFileSystemFacade() {
	}

	@Override
	protected abstract FileSystemFacade delegate();

	@Override
	public File root() {
		return delegate().root();
	}

	@Override
	public Collection<File> directories() {
		return delegate().directories();
	}

	@Override
	public File save(final DataHandler dataHandler, final String path) throws IOException {
		return delegate().save(dataHandler, path);
	}

}
