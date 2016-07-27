package org.cmdbuild.logic.menu;

import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore;

public class DefaultMenuLogic implements MenuLogic {

	private final MenuStore menuStore;

	public DefaultMenuLogic(final MenuStore menuStore) {
		this.menuStore = menuStore;
	}

	@Override
	public MenuItem read(final String group) {
		return menuStore.read(group);
	}

	@Override
	public MenuItem readAvailableItems(final String group) {
		return menuStore.getAvailableItems(group);
	}

	@Override
	public MenuItem readMenuWithPrivileges(final String group) {
		return menuStore.getMenuToUseForGroup(group);
	}

	@Override
	public void save(final String group, final MenuItem element) {
		menuStore.save(group, element);
	}

	@Override
	public void delete(final String group) {
		menuStore.delete(group);
	}

}
