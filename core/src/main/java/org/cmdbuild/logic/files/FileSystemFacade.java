package org.cmdbuild.logic.files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.activation.DataHandler;

public interface FileSystemFacade {

	File root();

	Collection<File> directories();

	File save(DataHandler dataHandler, String path) throws IOException;

}