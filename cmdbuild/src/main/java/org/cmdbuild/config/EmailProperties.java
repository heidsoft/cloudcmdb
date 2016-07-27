package org.cmdbuild.config;

import java.io.IOException;

import org.cmdbuild.services.Settings;

public class EmailProperties extends DefaultProperties implements EmailConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "email";

	private static final String QUEUE_ENABLED = "email.queue.enabled";
	private static final String QUEUE_TIME = "email.queue.time";

	public EmailProperties() {
		super();
		setEnabled(false);
		setQueueTime(0L);
	}

	public static EmailProperties getInstance() {
		return (EmailProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		return Boolean.parseBoolean(getProperty(QUEUE_ENABLED));
	}

	@Override
	public void setEnabled(final boolean enabled) {
		setProperty(QUEUE_ENABLED, Boolean.toString(enabled));
	}

	@Override
	public long getQueueTime() {
		return Long.valueOf(getProperty(QUEUE_TIME));
	}

	@Override
	public void setQueueTime(final long value) {
		setProperty(QUEUE_TIME, Long.toString(value));
	}

	@Override
	public void save() {
		try {
			store();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	};

}
