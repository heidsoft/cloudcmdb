package org.cmdbuild.logic.data;

import org.cmdbuild.config.CmdbuildConfiguration;

public class ConfigurationAwareLockLogic extends ForwardingLockLogic {

	private final CmdbuildConfiguration cmdbuildProperties;
	private final LockLogic whenNotConfigured;
	private final LockLogic whenConfigured;

	public ConfigurationAwareLockLogic(final CmdbuildConfiguration cmdbuildProperties,
			final LockLogic whenNotConfigured, final LockLogic whenConfigured) {
		this.cmdbuildProperties = cmdbuildProperties;
		this.whenNotConfigured = whenNotConfigured;
		this.whenConfigured = whenConfigured;
	}

	@Override
	protected LockLogic delegate() {
		return cmdbuildProperties.getLockCard() ? whenConfigured : whenNotConfigured;
	}

}
