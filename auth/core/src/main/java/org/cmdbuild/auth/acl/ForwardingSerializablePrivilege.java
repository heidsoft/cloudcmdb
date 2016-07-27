package org.cmdbuild.auth.acl;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingSerializablePrivilege extends ForwardingObject implements SerializablePrivilege {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingSerializablePrivilege() {
	}

	@Override
	protected abstract SerializablePrivilege delegate();

	@Override
	public String getPrivilegeId() {
		return delegate().getPrivilegeId();
	}

	@Override
	public Long getId() {
		return delegate().getId();
	}

	@Override
	public String getName() {
		return delegate().getName();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

}
