package org.cmdbuild.logic.taskmanager.scheduler;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.logic.taskmanager.TaskVisitor;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTask;
import org.cmdbuild.scheduler.Job;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

public class DefaultLogicAndSchedulerConverter implements LogicAndSchedulerConverter {

	static final Logger logger = TaskManagerLogic.logger;

	private static class DefaultLogicAsSourceConverter implements LogicAsSourceConverter, TaskVisitor {

		private final Map<Class<? extends ScheduledTask>, JobFactory<? extends ScheduledTask>> factories;
		private final ScheduledTask source;
		private final boolean execution;

		private Job job;

		public DefaultLogicAsSourceConverter(
				final Map<Class<? extends ScheduledTask>, JobFactory<? extends ScheduledTask>> factories,
				final ScheduledTask source, final boolean execution) {
			this.factories = factories;
			this.source = source;
			this.execution = execution;
		}

		@Override
		public LogicAsSourceConverter withNoExecution() {
			return new DefaultLogicAsSourceConverter(factories, source, false);
		}

		@Override
		public Job toJob() {
			source.accept(this);
			Validate.notNull(job, "conversion error");
			return job;
		}

		@Override
		public void visit(final AsynchronousEventTask task) {
			job = factories.get(task.getClass()).create(task, execution);
		}

		@Override
		public void visit(final ConnectorTask task) {
			job = factories.get(task.getClass()).create(task, execution);
		}

		@Override
		public void visit(final GenericTask task) {
			job = factories.get(task.getClass()).create(task, execution);
		}

		@Override
		public void visit(final ReadEmailTask task) {
			job = factories.get(task.getClass()).create(task, execution);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			job = factories.get(task.getClass()).create(task, execution);
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			throw new UnsupportedOperationException("invalid task " + task);
		}

	}

	private final Map<Class<? extends ScheduledTask>, JobFactory<? extends ScheduledTask>> factories;

	public DefaultLogicAndSchedulerConverter() {
		factories = Maps.newHashMap();
	}

	public <T extends ScheduledTask> void register(final Class<T> type, final JobFactory<T> factory) {
		factories.put(type, factory);
	};

	@Override
	public LogicAsSourceConverter from(final ScheduledTask source) {
		return new DefaultLogicAsSourceConverter(factories, source, true);
	}

}
