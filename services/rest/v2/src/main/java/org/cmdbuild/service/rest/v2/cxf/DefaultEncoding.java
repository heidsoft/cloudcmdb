package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.io.BaseEncoding.base64Url;

import org.cmdbuild.service.rest.v2.cxf.TranslatingAttachmentsHelper.Encoding;

import com.google.common.io.BaseEncoding;

public class DefaultEncoding implements Encoding {

	private final BaseEncoding encoding;

	public DefaultEncoding() {
		encoding = base64Url().omitPadding();
	}

	@Override
	public String encode(final String value) {
		return encoding.encode(value.getBytes());
	}

	@Override
	public String decode(final String value) {
		return new String(encoding.decode(value));
	}

}
