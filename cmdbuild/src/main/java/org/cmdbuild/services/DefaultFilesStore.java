package org.cmdbuild.services;

import static com.google.common.collect.FluentIterable.from;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.cmdbuild.utils.FilenameFilters.pattern;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

import com.google.common.base.Function;

public class DefaultFilesStore implements FilesStore {

	private static Function<File, String> FILE_NAME = new Function<File, String>() {

		@Override
		public String apply(final File input) {
			return input.getName();
		}

	};

	private final String relativeRootDirectory;
	private final String absoluteRootDirectory;
	private final File root;

	private static final String[] ALLOWED_IMAGE_TYPES = {
			"image/png", "image/gif", "image/jpeg", "image/pjpeg", "image/x-png" };

	public DefaultFilesStore(final String root, final String pathFromRoot) {
		this.relativeRootDirectory = pathFromRoot + separator;
		this.absoluteRootDirectory = root + separator + relativeRootDirectory;
		this.root = new File(absoluteRootDirectory);
	}

	@Override
	public FilesStore sub(final String dir) {
		return new DefaultFilesStore(absoluteRootDirectory, dir);
	}

	@Override
	public String[] list(final String dir) {
		return list(dir, null);
	}

	@Override
	public String[] list(final String dir, final String pattern) {
		return from(files(dir, pattern)) //
				.transform(FILE_NAME) //
				.toArray(String.class);
	}

	@Override
	public Iterable<File> files(final String dir, final String pattern) {
		return sub(dir).files(pattern);
	}

	@Override
	public Iterable<File> files(String pattern) {
		final File directory = new File(absoluteRootDirectory);
		final Iterable<File> output;
		if (directory.exists()) {
			output = asList(directory.listFiles(pattern(pattern)));
		} else {
			output = emptyList();
		}
		return output;
	}

	@Override
	public void remove(final String filePath) {
		final File theFile = new File(absoluteRootDirectory, filePath);
		if (theFile.exists()) {
			theFile.delete();
		} else {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		}
	}

	@Override
	public void rename(final String filePath, String newFilePath) {
		final File theFile = new File(absoluteRootDirectory, filePath);
		if (theFile.exists()) {
			final String extension = getExtension(theFile.getName());
			if (!"".equals(extension)) {
				newFilePath = newFilePath + extension;
			}

			final File newFile = newFile(absoluteRootDirectory, newFilePath);
			theFile.renameTo(newFile);
		} else {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		}
	}

	@Override
	public void save(final FileItem file, final String filePath) throws IOException {
		save(file.getInputStream(), filePath);
	}

	@Override
	public File save(final InputStream inputStream, final String filePath) throws IOException {
		FileOutputStream outputStream = null;
		try {
			final File destinationFile = newFile(absoluteRootDirectory, filePath);
			final File dir = destinationFile.getParentFile();
			dir.mkdirs();

			outputStream = new FileOutputStream(destinationFile);
			final byte[] buf = new byte[1024];
			int i = 0;
			while ((i = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, i);
			}
			return destinationFile;
		} catch (final FileNotFoundException e) {
			throw ORMExceptionType.ORM_ICONS_FILE_NOT_FOUND.createException();
		} catch (final IOException e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	private File newFile(final String parent, final String child) {
		final File destinationFile = new File(parent, child);
		if (destinationFile.exists()) {
			throw ORMExceptionType.ORM_ICONS_FILE_ALREADY_EXISTS.createException(destinationFile.getName());
		}
		return destinationFile;
	}

	@Override
	public String getRelativeRootDirectory() {
		return this.relativeRootDirectory;
	}

	@Override
	public String getAbsoluteRootDirectory() {
		return this.absoluteRootDirectory;
	}

	@Override
	public File getRoot() {
		return root;
	}

	@Override
	public boolean isImage(final FileItem file) {
		boolean valid = false;
		for (final String type : ALLOWED_IMAGE_TYPES) {
			if (type.equalsIgnoreCase(file.getContentType())) {
				valid = true;
				break;
			}
		}
		return valid;
	}

	@Override
	public String getExtension(final String fileName) {
		String ext = "";
		final int lastIndexOfPoint = fileName.lastIndexOf(".");
		if (lastIndexOfPoint >= 0) {
			ext = fileName.substring(lastIndexOfPoint);
		}
		return ext;
	}

	@Override
	public String removeExtension(final String fileName) {
		String cleanedFileName = fileName;
		final int lastIndexOfPoint = fileName.lastIndexOf(".");
		if (lastIndexOfPoint >= 0) {
			cleanedFileName = fileName.substring(0, lastIndexOfPoint);
		}
		return cleanedFileName;
	}

}
