package org.cmdbuild.services.store.menu;

import java.util.Map;

import org.cmdbuild.services.localization.LocalizableStorable;

public interface MenuElement extends LocalizableStorable {

	Long getId();

	String getCode();

	String getDescription();

	Integer getElementId();

	String getElementClassName();

	String getGroupName();

	Integer getNumber();

	Integer getParentId();

	MenuItemType getType();

	String getUuid();

	Map<String, Object> getSpecificTypeValues();

}
