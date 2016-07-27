package org.cmdbuild.logic.menu;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.services.store.menu.MenuItem;

public interface MenuLogic extends Logic {

	/*
	 * TODO change MenuItem to something specific of this layer
	 */

	MenuItem read(String group);

	MenuItem readAvailableItems(String group);

	MenuItem readMenuWithPrivileges(String group);

	void save(String group, MenuItem element);

	void delete(String group);

}
