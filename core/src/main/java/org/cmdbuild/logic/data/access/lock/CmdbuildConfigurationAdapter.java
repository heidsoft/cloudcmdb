package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.logic.data.access.lock.DefaultLockManager.DurationExpired;

public class CmdbuildConfigurationAdapter implements DurationExpired.Configuration {

	private final CmdbuildConfiguration delegate;

	public CmdbuildConfigurationAdapter(final CmdbuildConfiguration delegate) {
		this.delegate = delegate;
	}

	@Override
	public long getExpirationTimeInMilliseconds() {
		return delegate.getLockCardTimeOut() * 1000; // To have milliseconds
	}

}
