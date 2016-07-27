package org.cmdbuild.scheduler.command;

import org.cmdbuild.scheduler.logging.LoggingSupport;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class Commands {

	private static final Logger logger = LoggingSupport.logger;

	private static class NullCommand implements Command {

		@Override
		public void execute() {
			// nothing to do
		}

	}

	private static final NullCommand INSTANCE = new NullCommand();

	public static Command nullCommand() {
		return INSTANCE;
	}

	private static class SafeCommand implements Command {

		private final Command delegate;

		private SafeCommand(final Command delegate) {
			this.delegate = delegate;
		}

		@Override
		public void execute() {
			try {
				delegate.execute();
			} catch (final Throwable e) {
				logger.warn("error executing command", e);
			}
		}

	}

	public static Command safe(final Command delegate) {
		return new SafeCommand(delegate);
	}

	private static class ComposeOnExeption extends ForwardingCommand {

		private static final Logger logger = LoggingSupport.logger;
		private static final Marker marker = MarkerFactory.getMarker(ComposeOnExeption.class.getName());

		private final Command delegate;
		private final Command onException;

		public ComposeOnExeption(final Command delegate, final Command onException) {
			this.delegate = delegate;
			this.onException = onException;
		}

		@Override
		protected Command delegate() {
			return delegate;
		}

		@Override
		public void execute() {
			try {
				super.execute();
			} catch (final Exception e) {
				logger.warn(marker, "error executing command", e);
				onException.execute();
			}
		}

	}

	public static Command composeOnExeption(final Command delegate, final Command onException) {
		return new ComposeOnExeption(delegate, onException);
	}

	private static class ConditionalCommand implements Command {

		private final Command delegate;
		private final Predicate<Void> predicate;

		public ConditionalCommand(final Command delegate, final Predicate<Void> predicate) {
			this.delegate = delegate;
			this.predicate = predicate;
		}

		@Override
		public void execute() {
			if (predicate.apply(null)) {
				delegate.execute();
			}
		}

	}

	public static Command conditional(final Command delegate, final Predicate<Void> predicate) {
		return new ConditionalCommand(delegate, predicate);
	}

	private static class CompositeCommand implements Command {

		private final Iterable<Command> delegates;

		public CompositeCommand(final Iterable<Command> delegates) {
			this.delegates = delegates;
		}

		@Override
		public void execute() {
			for (final Command element : delegates) {
				element.execute();
			}
		}

	}

	public static Command composite(final Iterable<Command> delegates) {
		return new CompositeCommand(delegates);
	}

	private Commands() {
		// prevents instantiation
	}

}
