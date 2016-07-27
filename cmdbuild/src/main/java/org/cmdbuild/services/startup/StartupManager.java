package org.cmdbuild.services.startup;

import com.google.common.base.Predicate;

/**
 * Handles the startup of multiple {@link Startable} objects according to some
 * {@link Condition}s.
 * 
 * @since 2.2
 */
public interface StartupManager {

	interface Startable {

		void start();

	}

	/**
	 * Adds a {@link Startable} object with a specific condition ({@link Predicate}).
	 * 
	 * @param startable
	 * @param condition
	 */
	void add(Startable startable, Predicate<Void> condition);

	/**
	 * Starts all {@link Startable} objects whose specific {@link Condition} is
	 * satisfied and it makes sure that those objects are not started again.
	 */
	void start();

}
