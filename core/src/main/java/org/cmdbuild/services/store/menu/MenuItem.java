package org.cmdbuild.services.store.menu;

import java.util.List;
import java.util.Map;

public interface MenuItem {
	/**
	 * 
	 * @return the Id of the menu item
	 */
	Long getId();

	/**
	 * 
	 * @return the type of the menu item
	 */
	MenuItemType getType();

	/**
	 * 
	 * @return the Label to use to describe the menu item
	 */
	String getDescription();

	/**
	 * 
	 * @return the id of the menu item that is the parent of this item
	 */
	Integer getParentId();

	/**
	 * 
	 * @return the name of the class to open, if the menu is associated to a
	 *         class/process. Otherwise, this is the name of a class and you
	 *         have to use also the {@link getElementId}
	 */
	String getReferedClassName();

	/**
	 * 
	 * @return the id of the element that the menu item point to. Currently is
	 *         used for the report. Must be used also for dashboards and views
	 */
	Number getReferencedElementId();

	/**
	 * 
	 * @return a number to sort the menu items
	 */
	int getIndex();

	/**
	 * 
	 * @return the name of the group that owns this menu
	 */
	String getGroupName();

	/**
	 * 
	 * @return the children menu item of this item
	 */
	List<MenuItem> getChildren();

	/**
	 * 
	 * @return a map with type dependent values
	 */
	Map<String, Object> getSpecificTypeValues();

	void setId(Long id);

	void setType(MenuItemType type);

	void setDescription(String description);

	void setParentId(Integer parentId);

	void setReferedClassName(String referencedClassName);

	void setReferencedElementId(Number referencedElementId);

	void setIndex(int index);

	void setGroupName(String groupName);

	void addChild(MenuItem child);

	void setSpecificTypeValues(Map<String, Object> specificTypeValues);

	void sortChildByIndex();

	void sortChildByDescription();

	String getUniqueIdentifier();

	void setUniqueIdentifier(String uuid);
}