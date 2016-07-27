package org.cmdbuild.logic.email;

import org.cmdbuild.logic.Logic;

public interface EmailQueueLogic extends Logic {

	interface Configuration {

		long time();

	}

	boolean running();

	void start();

	void stop();

	Configuration configuration();

	void configure(Configuration configuration);

}
