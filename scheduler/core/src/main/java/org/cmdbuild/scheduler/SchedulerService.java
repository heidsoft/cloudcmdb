package org.cmdbuild.scheduler;

public interface SchedulerService {

	void add(Job job, Trigger trigger);

	void remove(Job job);

	boolean isStarted(Job job);

	void start();

	void stop();

}
