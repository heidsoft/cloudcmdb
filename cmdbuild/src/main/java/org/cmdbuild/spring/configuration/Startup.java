package org.cmdbuild.spring.configuration;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.and;

import java.util.Collections;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.services.startup.DefaultStartupLogic;
import org.cmdbuild.services.startup.DefaultStartupManager;
import org.cmdbuild.services.startup.StartupLogic;
import org.cmdbuild.services.startup.StartupManager;
import org.cmdbuild.services.startup.StartupManager.Startable;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

@Configuration
public class Startup {

	private static final Predicate<Void> ALWAYS = alwaysTrue();

	@Autowired
	private Cache cache;

	@Autowired
	private Dms dms;

	@Autowired
	private Email email;

	@Autowired
	private Migration migration;

	@Autowired
	private Properties properties;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private TaskManager taskManager;

	@Bean
	public StartupLogic startupLogic() {
		return new DefaultStartupLogic( //
				startupManager(), //
				migration.patchManager(), //
				cache.defaultCachingLogic() //
		);
	}

	@Bean
	protected StartupManager startupManager() {
		final StartupManager startupManager = new DefaultStartupManager();
		startupManager.add(startScheduler(), databaseIsOk());
		/*
		 * needed because some Java compilers are not able to deduce generic
		 * type
		 */
		final Predicate<Void> and = and(databaseIsOk(), emailQueueEnabled());
		startupManager.add(startEmailQueue(), and);
		startupManager.add(clearDmsTemporaryFolder(), ALWAYS);
		return startupManager;
	}

	@Bean
	protected Startable startScheduler() {
		return new Startable() {

			private final SchedulerLogic schedulerLogic = scheduler.defaultSchedulerLogic();
			private final TaskManagerLogic taskManagerLogic = taskManager.taskManagerLogic();

			@Override
			public void start() {
				schedulerLogic.startScheduler();

				for (final Task task : taskManagerLogic.read()) {
					if (task.isActive()) {
						try {
							taskManagerLogic.logger.debug("starting task '{}'", task.getId());
							taskManagerLogic.activate(task.getId());
						} catch (final Exception e) {
							taskManagerLogic.logger.error("task '{}' cannot be started due to an error", task.getId());
							taskManagerLogic.logger.error("error starting task", e);
						}
					}
				}
			}

		};
	}

	@Bean
	protected Startable startEmailQueue() {
		return new Startable() {

			@Override
			public void start() {
				email.emailQueue().start();
			}

		};
	}

	@Bean
	protected Startable clearDmsTemporaryFolder() {
		return new Startable() {

			private final Logger logger = Log.CMDBUILD;

			/*
			 * we need to call it now, even if not used, because DmsService will
			 * be configured when injected inside DmsLogic
			 */
			@SuppressWarnings("unused")
			private final DmsLogic dmsLogic = dms.defaultDmsLogic();
			private final DmsConfiguration dmsConfiguration = properties.dmsProperties();
			private final DmsService dmsService = dms.dmsService();
			private final DocumentCreatorFactory documentCreatorFactory = dms.documentCreatorFactory();

			private final Iterable<String> ROOT = Collections.emptyList();

			@Override
			public void start() {
				logger.info("clearing DMS temporary folder");
				try {
					if (dmsConfiguration.isEnabled()) {
						final DocumentSearch all = documentCreatorFactory.createTemporary(ROOT) //
								.createDocumentSearch(null, null);
						dmsService.delete(all);
					}
				} catch (final Throwable e) {
					logger.warn("error clearing DMS temporary", e);
				}
			}

		};
	}

	@Bean
	protected Predicate<Void> databaseIsOk() {
		return new Predicate<Void>() {

			@Override
			public boolean apply(final Void input) {
				return properties.databaseProperties().isConfigured() && migration.patchManager().isUpdated();
			}

		};
	}

	@Bean
	protected Predicate<Void> emailQueueEnabled() {
		return new Predicate<Void>() {

			@Override
			public boolean apply(final Void input) {
				return properties.emailProperties().isEnabled();
			}

		};
	}

}
