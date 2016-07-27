package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.CommunicationConstants.GROUP_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.MENU;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.MenuSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class ModMenu extends JSONBaseWithSpringContext {

	private static final boolean WITH_WRAPPER = true;
	private static final boolean SORT_BY_DESCRIPTION = true;

	/**
	 * 
	 * @param groupName
	 * @return The full menu configuration. All the MenuItems configured for the
	 *         given group name.
	 * @throws JSONException
	 * @throws AuthException
	 * @throws NotFoundException
	 * @throws ORMException
	 */
	@Admin
	@JSONExported
	public JSONObject getMenuConfiguration( //
			@Parameter(GROUP_NAME) final String groupName //
	) throws JSONException, AuthException, NotFoundException, ORMException {
		final MenuItem menu = menuLogic().read(groupName);
		final MenuSerializer menuSerializer = MenuSerializer.newInstance() //
				.withRootItem(menu) //
				.withTranslationFacade(translationFacade()) //
				.withDataView(systemDataView()) //
				.build();
		return menuSerializer.toClient(WITH_WRAPPER);
	}

	/**
	 * 
	 * @param groupName
	 *            The group for which we want the items that could be added to
	 *            the menu. This items are Classes, Processes, Reports and
	 *            Dashboards
	 * 
	 * @return the list of available items grouped by type
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public JSONObject getAvailableMenuItems( //
			@Parameter(GROUP_NAME) final String groupName //
	) throws JSONException {
		final MenuItem availableMenu = menuLogic().readAvailableItems(groupName);
		final MenuSerializer menuSerializer = MenuSerializer.newInstance() //
				.withRootItem(availableMenu) //
				.withTranslationFacade(translationFacade()) //
				.withDataView(systemDataView()) //
				.build();
		return menuSerializer.toClient(availableMenu, WITH_WRAPPER, SORT_BY_DESCRIPTION);
	}

	/**
	 * 
	 * @param groupName
	 *            the group name for which we want save the menu
	 * @param jsonMenuItems
	 *            the list of menu items
	 * @throws Exception
	 */
	@Admin
	@JSONExported
	public void saveMenu( //
			@Parameter(GROUP_NAME) final String groupName, //
			@Parameter(MENU) final JSONObject jsonMenu //
	) throws Exception {
		final MenuItem menu = MenuSerializer.toServer(jsonMenu);
		menuLogic().save(groupName, menu);
	}

	/**
	 * 
	 * @param groupName
	 *            the name of the group for which we want delete the menu
	 * @return
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public void deleteMenu( //
			@Parameter(GROUP_NAME) final String groupName //
	) throws JSONException {
		menuLogic().delete(groupName);
	}

	/**
	 * 
	 * @param groupName
	 * @return the menu defined for the given group. If there are no menu for
	 *         this group, it returns the DefaultMenu (if exists). Note that
	 *         this method has to remove, eventually, the nodes that point to
	 *         something that the user has not the privileges to manage
	 * @throws JSONException
	 * @throws AuthException
	 * @throws NotFoundException
	 * @throws ORMException
	 */
	@JSONExported
	public JSONObject getAssignedMenu( //
			@Parameter(GROUP_NAME) final String groupName //
	) throws JSONException {
		final MenuItem menu = menuLogic().readMenuWithPrivileges(groupName);
		final MenuSerializer menuSerializer = MenuSerializer.newInstance() //
				.withRootItem(menu) //
				.withTranslationFacade(translationFacade()) //
				.withDataView(systemDataView()) //
				.build();
		return menuSerializer.toClient(WITH_WRAPPER);
	}

}
