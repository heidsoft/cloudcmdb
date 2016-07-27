package org.cmdbuild.services.store.menu;

import org.cmdbuild.data.store.Store;
import org.slf4j.Logger;

public interface MenuStore {

	Logger logger = Store.logger;

	/**
	 * @param groupName
	 * @return the menu defined for the given group. If the group name is an
	 *         empty string or null retrieve a Default Menu
	 */
	MenuItem read(String groupName);

	/**
	 * @param groupName
	 *            the group for this menu
	 * @param menuItem
	 *            the menu to save
	 */
	void save(String groupName, MenuItem menuItem);

	/**
	 * 
	 * @param groupName
	 *            the group for which delete the menu
	 */
	void delete(String groupName);

	/**
	 * 
	 * @param groupName
	 * @return a menu that has as top level children the categories of menuItems
	 *         (Classes, Activities, Report, Dashboards and View) The children
	 *         of these nodes are the respective nodes that are not yet added to
	 *         the current menu
	 */
	MenuItem getAvailableItems(String groupName);

	/**
	 * 
	 * @param groupName
	 * @return the menu configured for the group with the given name or the
	 *         default menu if there is not one. In any case, for this menu must
	 *         remove the items for which the group has not the read privileges
	 */
	MenuItem getMenuToUseForGroup(String groupName);

}
