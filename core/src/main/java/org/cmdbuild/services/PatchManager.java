package org.cmdbuild.services;

public interface PatchManager {

	public interface Patch {

		String getVersion();

		String getDescription();

		String getCategory();

	}

	void reset();

	void applyPatchList();

	Iterable<Patch> getAvaiblePatch();

	boolean isUpdated();

	/**
	 * Used within DatabaseConfigurator to set updated a new Database.
	 */
	void createLastPatch();

}
