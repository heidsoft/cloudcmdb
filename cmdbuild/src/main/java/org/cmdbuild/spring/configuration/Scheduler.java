package org.cmdbuild.spring.configuration;

import org.cmdbuild.logic.scheduler.DefaultSchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.quartz.QuartzSchedulerService;
import org.cmdbuild.services.scheduler.DefaultSchedulerExeptionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Scheduler {

	@Bean
	public SchedulerLogic defaultSchedulerLogic() {
		return new DefaultSchedulerLogic(defaultSchedulerService());
	}

	@Bean
	public SchedulerService defaultSchedulerService() {
		return new QuartzSchedulerService(schedulerExeptionFactory());
	}

	@Bean
	protected SchedulerExeptionFactory schedulerExeptionFactory() {
		return new DefaultSchedulerExeptionFactory();
	}

}
