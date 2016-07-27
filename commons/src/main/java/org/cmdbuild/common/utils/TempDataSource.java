package org.cmdbuild.common.utils;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.logging.LoggingSupport;

public class TempDataSource implements DataSource, LoggingSupport {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<TempDataSource> {

		private String name;
		private String contentType;
		private File file;

		private Builder() {
			// use factory method
		}

		@Override
		public TempDataSource build() {
			validate();
			return new TempDataSource(this);
		}

		private void validate() {
			Validate.notBlank(name, "invalid file name");
			try {
				file = File.createTempFile(PREFIX, name);
				file.deleteOnExit();
			} catch (final Exception e) {
				logger.error("error creating temporary file");
				throw new RuntimeException(e);
			}
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withContentType(final String contentType) {
			this.contentType = contentType;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final String PREFIX = "tempdatasource";

	private final String name;
	private final String contentType;
	private final File file;

	private TempDataSource(final Builder builder) {
		this.name = builder.name;
		this.contentType = builder.contentType;
		this.file = builder.file;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getContentType() {
		return defaultIfNull(contentType, new MimetypesFileTypeMap().getContentType(file));
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(file);
	}

	public File getFile() {
		return file;
	}

}
