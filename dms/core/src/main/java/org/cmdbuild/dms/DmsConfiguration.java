package org.cmdbuild.dms;

import java.util.EventListener;

public interface DmsConfiguration {

	static interface ChangeListener extends EventListener {

		void configurationChanged();

	}

	void addListener(ChangeListener listener);

	boolean isEnabled();

	String getService();

	String getCmdbuildCategory();

	/**
	 * Returns the content of the file containing the custom model definition.
	 * 
	 * @return the content of the file or an empty string if the file doesn't
	 *         exist or there was an error during file access.
	 * 
	 */
	String getCustomModelFileContent();

	String getAlfrescoCustomUri();

	/**
	 * Returns the content of the file containing the custom model definition.
	 * 
	 * @return the content of the file or an empty string if the file doesn't
	 *         exist or there was an error during file access.
	 * 
	 */
	String getMetadataAutocompletionFileContent();

}
