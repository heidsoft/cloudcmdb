package org.cmdbuild.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;

public interface FilesStore {

	FilesStore sub(String dir);

	String[] list(String dir);

	String[] list(String dir, String pattern);

	Iterable<File> files(String dir, String pattern);

	Iterable<File> files(String pattern);

	void remove(String filePath);

	void rename(String filePath, String newFilePath);

	void save(FileItem file, String filePath) throws IOException;

	File save(InputStream inputStream, String filePath) throws IOException;

	String getRelativeRootDirectory();

	String getAbsoluteRootDirectory();

	File getRoot();

	boolean isImage(FileItem file);

	String getExtension(String fileName);

	String removeExtension(String fileName);

}
