package org.cmdbuild.services.startup;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Predicate;

public class DefaultStartupManager implements StartupManager {

	private final Map<Startable, Predicate<Void>> startables;

	public DefaultStartupManager() {
		startables = newHashMap();
	}

	@Override
	public void add(final Startable startable, final Predicate<Void> condition) {
		startables.put(startable, condition);
	}

	@Override
	public void start() {
		final Collection<Startable> started = newArrayList();
		for (final Startable startable : startables.keySet()) {
			if (startables.get(startable).apply(null)) {
				startable.start();
				started.add(startable);
			}
		}
		for (final Startable startable : started) {
			startables.remove(startable);
		}
	}

}
