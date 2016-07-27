package org.cmdbuild.dao.entrytype;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingEntryType extends ForwardingObject implements CMEntryType {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingEntryType() {
	}

	@Override
	protected abstract CMEntryType delegate();

	@Override
	public boolean isActive() {
		return delegate().isActive();
	}

	@Override
	public String getPrivilegeId() {
		return delegate().getPrivilegeId();
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		delegate().accept(visitor);
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
	public CMIdentifier getIdentifier() {
		return delegate().getIdentifier();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public boolean isSystem() {
		return delegate().isSystem();
	}

	@Override
	public boolean isSystemButUsable() {
		return delegate().isSystemButUsable();
	}

	@Override
	public boolean isBaseClass() {
		return delegate().isBaseClass();
	}

	@Override
	public boolean holdsHistory() {
		return delegate().holdsHistory();
	}

	@Override
	public Iterable<? extends CMAttribute> getActiveAttributes() {
		return delegate().getActiveAttributes();
	}

	@Override
	public Iterable<? extends CMAttribute> getAttributes() {
		return delegate().getAttributes();
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		return delegate().getAllAttributes();
	}

	@Override
	public CMAttribute getAttribute(final String name) {
		return delegate().getAttribute(name);
	}

	@Override
	public String getKeyAttributeName() {
		return delegate().getKeyAttributeName();
	}

	@Override
	public int hashCode() {
		return delegate().hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return delegate().equals(obj);
	}

}
