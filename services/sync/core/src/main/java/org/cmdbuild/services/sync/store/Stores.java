package org.cmdbuild.services.sync.store;

import org.cmdbuild.services.sync.logging.LoggingSupport;

public class Stores {

	private static class LoggingStore extends ForwardingStore implements LoggingSupport {

		private final Store delegate;

		public LoggingStore(final Store delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Store delegate() {
			return delegate;
		}

		@Override
		public void create(final Entry entry) {
			try {
				logger.debug("creating entry '{}'", entry);
				super.create(entry);
			} catch (final RuntimeException e) {
				logger.error("error creating entry", e);
				throw e;
			}
		}

		@Override
		public Iterable<Entry> readAll() {
			try {
				logger.debug("reading all entries");
				return super.readAll();
			} catch (final RuntimeException e) {
				logger.error("error reading all entries", e);
				throw e;
			}
		}

		@Override
		public void update(final Entry entry) {
			try {
				logger.debug("updating entry '{}'", entry);
				super.update(entry);
			} catch (final RuntimeException e) {
				logger.error("error updating entry", e);
				throw e;
			}
		}

		@Override
		public void delete(final Entry entry) {
			try {
				logger.debug("deleting entry '{}'", entry);
				super.delete(entry);
			} catch (final RuntimeException e) {
				logger.error("error deleting entry", e);
				throw e;
			}
		}

	}

	public static Store logging(final Store delegate) {
		return new LoggingStore(delegate);
	}

	private Stores() {
		// prevents instantiation
	}

}
