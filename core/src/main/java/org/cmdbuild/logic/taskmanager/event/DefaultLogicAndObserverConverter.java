package org.cmdbuild.logic.taskmanager.event;

import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.services.event.Observer;

public class DefaultLogicAndObserverConverter implements LogicAndObserverConverter {

	private static class DefaultLogicAsSourceConverter implements LogicAsSourceConverter {

		private final SynchronousEventTask source;
		private final ObserverFactory observerFactory;

		public DefaultLogicAsSourceConverter(final ObserverFactory observerFactory, final SynchronousEventTask source) {
			this.source = source;
			this.observerFactory = observerFactory;
		}

		@Override
		public Observer toObserver() {
			return observerFactory.create(source);
		}
	}

	private final ObserverFactory observerFactory;

	public DefaultLogicAndObserverConverter(final ObserverFactory observerFactory) {
		this.observerFactory = observerFactory;
	}

	@Override
	public LogicAsSourceConverter from(final SynchronousEventTask source) {
		return new DefaultLogicAsSourceConverter(observerFactory, source);
	}

}
