package org.cmdbuild.data.store.custompage;

import java.util.Collection;

import org.cmdbuild.data.store.ForwardingStore;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class CustomPagesStore extends ForwardingStore<DBCustomPage> {

	private static final Marker MARKER = MarkerFactory.getMarker(CustomPagesStore.class.getName());

	public static interface Synchronizer {

		Logger logger = CustomPagesStore.logger;

		void synchronize();

	}

	private final Store<DBCustomPage> delegate;
	private final Synchronizer synchronizer;

	public CustomPagesStore(final Store<DBCustomPage> delegate, final Synchronizer synchronizer) {
		this.delegate = delegate;
		this.synchronizer = synchronizer;
	}

	@Override
	protected Store<DBCustomPage> delegate() {
		return delegate;
	}

	@Override
	public DBCustomPage read(final Storable storable) {
		synchronize();
		return super.read(storable);
	}

	@Override
	public Collection<DBCustomPage> readAll() {
		synchronize();
		return super.readAll();
	}

	@Override
	public Collection<DBCustomPage> readAll(final Groupable groupable) {
		synchronize();
		return super.readAll(groupable);
	}

	private synchronized void synchronize() {
		logger.debug(MARKER, "synchronizing");
		synchronizer.synchronize();
	}

}
