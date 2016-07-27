package org.cmdbuild.services.store.menu;

import java.util.Map;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingMenuElement extends ForwardingObject implements MenuElement {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingMenuElement() {
	}

	@Override
	protected abstract MenuElement delegate();

	@Override
	public String getIdentifier() {
		return delegate().getIdentifier();
	}

	@Override
	public Long getId() {
		return delegate().getId();
	}

	@Override
	public String getCode() {
		return delegate().getCode();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public MenuItemType getType() {
		return delegate().getType();
	}

	@Override
	public Integer getParentId() {
		return delegate().getParentId();
	}

	@Override
	public Integer getNumber() {
		return delegate().getNumber();
	}

	@Override
	public Integer getElementId() {
		return delegate().getElementId();
	}

	@Override
	public String getGroupName() {
		return delegate().getGroupName();
	}

	@Override
	public String getUuid() {
		return delegate().getUuid();
	}

	@Override
	public String getElementClassName() {
		return delegate().getElementClassName();
	}

	@Override
	public Map<String, Object> getSpecificTypeValues() {
		return delegate().getSpecificTypeValues();
	}

}
