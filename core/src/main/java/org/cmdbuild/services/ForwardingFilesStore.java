package org.cmdbuild.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingFilesStore extends ForwardingObject implements FilesStore {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingFilesStore() {
	}

	@Override
	protected abstract FilesStore delegate();

	@Override
	public FilesStore sub(final String dir) {
		return delegate().sub(dir);
	}

	@Override
	public String[] list(final String dir) {
		return delegate().list(dir);
	}

	@Override
	public String[] list(final String dir, final String pattern) {
		return delegate().list(dir, pattern);
	}

	@Override
	public Iterable<File> files(final String dir, final String pattern) {
		return delegate().files(dir, pattern);
	}

	@Override
	public Iterable<File> files(final String pattern) {
		return delegate().files(pattern);
	}

	@Override
	public void remove(final String filePath) {
		delegate().remove(filePath);
	}

	@Override
	public void rename(final String filePath, final String newFilePath) {
		delegate().rename(filePath, newFilePath);
	}

	@Override
	public void save(final FileItem file, final String filePath) throws IOException {
		delegate().save(file, filePath);
	}

	@Override
	public File save(final InputStream inputStream, final String filePath) throws IOException {
		return delegate().save(inputStream, filePath);
	}

	@Override
	public String getRelativeRootDirectory() {
		return delegate().getRelativeRootDirectory();
	}

	@Override
	public String getAbsoluteRootDirectory() {
		return delegate().getAbsoluteRootDirectory();
	}

	@Override
	public File getRoot() {
		return delegate().getRoot();
	}

	@Override
	public boolean isImage(final FileItem file) {
		return delegate().isImage(file);
	}

	@Override
	public String getExtension(final String fileName) {
		return delegate().getExtension(fileName);
	}

	@Override
	public String removeExtension(final String fileName) {
		return delegate().removeExtension(fileName);
	}

}
