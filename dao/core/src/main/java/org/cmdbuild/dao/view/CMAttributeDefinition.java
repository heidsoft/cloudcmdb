package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

/**
 * Attribute definition used for creating or updating attributes.
 */
public interface CMAttributeDefinition {

	String getName();

	CMEntryType getOwner();

	CMAttributeType<?> getType();

	String getDescription();

	String getDefaultValue();

	Boolean isDisplayableInList();

	boolean isMandatory();

	boolean isUnique();

	Boolean isActive();

	Mode getMode();

	Integer getIndex();

	String getGroup();

	Integer getClassOrder();

	String getEditorType();

	String getFilter();

	String getForeignKeyDestinationClassName();

}
