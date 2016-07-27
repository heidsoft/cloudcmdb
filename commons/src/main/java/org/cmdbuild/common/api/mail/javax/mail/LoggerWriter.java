package org.cmdbuild.common.api.mail.javax.mail;

import java.io.Writer;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

class LoggerWriter extends Writer {

	private final Logger logger;

	public LoggerWriter(final Logger logger) {
		Validate.notNull(logger, "missing logger");
		this.logger = logger;
	}

	@Override
	public void write(final char[] buffer, final int offset, final int length) {
		final StringBuilder _buffer = new StringBuilder();
		for (int i = 0; i < length; i++) {
			final char ch = buffer[offset + i];
			if (ch == '\n' && _buffer.length() > 0) {
				this.logger.info(_buffer.toString());
				_buffer.setLength(0);
			} else {
				_buffer.append(ch);
			}
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}

}