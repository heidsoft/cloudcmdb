package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingAttributeDefinition extends ForwardingObject implements CMAttributeDefinition {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingAttributeDefinition() {
	}

	@Override
	protected abstract CMAttributeDefinition delegate();

	@Override
	public String getName() {
		return delegate().getName();
	}

	@Override
	public CMEntryType getOwner() {
		return delegate().getOwner();
	}

	@Override
	public CMAttributeType<?> getType() {
		return delegate().getType();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public String getDefaultValue() {
		return delegate().getDefaultValue();
	}

	@Override
	public Boolean isDisplayableInList() {
		return delegate().isDisplayableInList();
	}

	@Override
	public boolean isMandatory() {
		return delegate().isMandatory();
	}

	@Override
	public boolean isUnique() {
		return delegate().isMandatory();
	}

	@Override
	public Boolean isActive() {
		return delegate().isActive();
	}

	@Override
	public Mode getMode() {
		return delegate().getMode();
	}

	@Override
	public Integer getIndex() {
		return delegate().getIndex();
	}

	@Override
	public String getGroup() {
		return delegate().getGroup();
	}

	@Override
	public Integer getClassOrder() {
		return delegate().getClassOrder();
	}

	@Override
	public String getEditorType() {
		return delegate().getEditorType();
	}

	@Override
	public String getFilter() {
		return delegate().getFilter();
	}

	@Override
	public String getForeignKeyDestinationClassName() {
		return delegate().getForeignKeyDestinationClassName();
	}

}
