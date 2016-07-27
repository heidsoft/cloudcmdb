package org.cmdbuild.services;

import java.io.File;

import org.cmdbuild.services.DefaultPatchManager.Repository;

public class FilesStoreRepository implements Repository {

	private final FilesStore delegate;
	private final String category;

	public FilesStoreRepository(final FilesStore delegate, final String category) {
		this.delegate = delegate;
		this.category = category;
	}

	@Override
	public Iterable<File> getFiles(final String pattern) {
		return delegate.files(pattern);
	}

	@Override
	public String getCategory() {
		return category;
	}

}
