package org.cmdbuild.logic.files;

import java.util.Optional;

import javax.activation.DataHandler;

public interface FileStore {

	Iterable<Element> folders();

	Optional<Element> folder(String folder);

	// TODO change String to Element
	Iterable<Element> files(String folder);

	// TODO change String to Element
	Optional<Element> create(String folder, DataHandler dataHandler);

	Optional<DataHandler> download(Element file);

	void delete(Element file);

}
