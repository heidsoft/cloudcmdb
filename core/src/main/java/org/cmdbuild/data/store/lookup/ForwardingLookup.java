package org.cmdbuild.data.store.lookup;

import org.cmdbuild.services.localization.LocalizableStorableVisitor;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingLookup extends ForwardingObject implements Lookup {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingLookup() {
	}

	@Override
	protected abstract Lookup delegate();

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String code() {
		return delegate().code();
	}

	@Override
	public String description() {
		return delegate().description();
	}

	@Override
	public String notes() {
		return delegate().notes();
	}

	@Override
	public LookupType type() {
		return delegate().type();
	}

	@Override
	public Integer number() {
		return delegate().number();
	}

	@Override
	public boolean active() {
		return delegate().active();
	}

	@Override
	public boolean isDefault() {
		return delegate().isDefault();
	}

	@Override
	public Long parentId() {
		return delegate().parentId();
	}

	@Override
	public Lookup parent() {
		return delegate().parent();
	}

	@Override
	public String uuid() {
		return delegate().uuid();
	}

	@Override
	public String getIdentifier() {
		return delegate().getIdentifier();
	}

	@Override
	public String getTranslationUuid() {
		return delegate().getTranslationUuid();
	}

	@Override
	public Long getId() {
		return delegate().getId();
	}

	@Override
	public void setId(final Long id) {
		delegate().setId(id);
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

}
