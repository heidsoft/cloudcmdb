package org.cmdbuild.config;

public interface EmailConfiguration {

	boolean isEnabled();

	void setEnabled(boolean enabled);

	long getQueueTime();

	void setQueueTime(long value);

	void save();

}
