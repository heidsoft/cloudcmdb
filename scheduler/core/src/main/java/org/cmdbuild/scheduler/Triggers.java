package org.cmdbuild.scheduler;


public class Triggers {

	private static final String EVERY_SECOND = "0/1 * * * * ?";
	private static final String EVERY_MINUTE = "0 * * * * ?";

	public static Trigger everySecond() {
		return RecurringTrigger.at(EVERY_SECOND);
	}

	public static Trigger everyMinute() {
		return RecurringTrigger.at(EVERY_MINUTE);
	}

	private Triggers() {
		// prevents instantiation
	}

}
