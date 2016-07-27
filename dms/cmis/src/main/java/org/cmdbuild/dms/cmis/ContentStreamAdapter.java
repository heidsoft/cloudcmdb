package org.cmdbuild.dms.cmis;

import static org.apache.commons.io.IOUtils.copyLarge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.chemistry.opencmis.commons.data.ContentStream;

public class ContentStreamAdapter implements DataSource {

	private final ContentStream contentStream;
	private final byte[] buffer;

	public ContentStreamAdapter(final ContentStream contentStream) throws IOException {
		this.contentStream = contentStream;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		copyLarge(contentStream.getStream(), output);
		buffer = output.toByteArray();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(buffer);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public String getContentType() {
		return contentStream.getMimeType();
	}

	@Override
	public String getName() {
		return contentStream.getFileName();
	}

}
