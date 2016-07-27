package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.services.store.menu.MenuItemDTO;
import org.cmdbuild.services.store.menu.MenuItemType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuSerializer {

	public static final String MENU = "menu", //
			CHILDREN = "children", //
			CLASS_NAME = "referencedClassName", //
			ELEMENT_ID = "referencedElementId", //
			INDEX = "index", //
			SPECIFIC_TYPE_VALUES = "specificTypeValues", //
			TYPE = "type", //
			UUID = "uuid";

	private final MenuItem rootItem;

	private MenuSerializer(final Builder builder) {
		this.rootItem = builder.rootItem;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<MenuSerializer> {

		MenuItem rootItem;
		TranslationFacade translationFacade;
		CMDataView dataView;

		@Override
		public MenuSerializer build() {
			return new MenuSerializer(this);
		}

		public Builder withRootItem(final MenuItem rootItem) {
			this.rootItem = rootItem;
			return this;
		}

		public Builder withTranslationFacade(final TranslationFacade translationFacade) {
			this.translationFacade = translationFacade;
			return this;
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}
	}

	/**
	 * Serialize the menu as tree sorting the items by index
	 * 
	 * @param menu
	 * @param withWrapper
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toClient( //
			final boolean withWrapper //
	) throws JSONException {
		final boolean sortByDescription = false;
		return toClient(rootItem, withWrapper, sortByDescription);
	}

	/**
	 * Serialize the menu as tree. If sortByDescription is false sort the items
	 * by index
	 * 
	 * @param menu
	 * @param withWrapper
	 * @param sortByDescription
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toClient( //
			final MenuItem menu, //
			final boolean withWrapper, //
			final boolean sortByDescription //
	) throws JSONException {

		final JSONObject out = singleToClient(menu);
		if (menu.getChildren().size() > 0) {
			final JSONArray children = new JSONArray();

			if (sortByDescription) {
				menu.sortChildByDescription();
			} else {
				menu.sortChildByIndex();
			}

			for (final MenuItem child : menu.getChildren()) {
				children.put(toClient(child, false, sortByDescription));
			}
			out.put(CHILDREN, children);
		}

		if (withWrapper) {
			return new JSONObject() {
				{
					put(MENU, out);
				}
			};
		} else {
			return out;
		}
	}

	public JSONObject singleToClient(final MenuItem menuItem) throws JSONException {
		final JSONObject out = new JSONObject();
		final String elementType = menuItem.getType().getValue();
		out.put(TYPE, elementType);
		out.put(INDEX, menuItem.getIndex());
		out.put(ELEMENT_ID, menuItem.getReferencedElementId());
		out.put(UUID, menuItem.getUniqueIdentifier());
		out.put(DESCRIPTION, menuItem.getDescription());
		out.put(CLASS_NAME, menuItem.getReferedClassName());
		out.put(SPECIFIC_TYPE_VALUES, menuItem.getSpecificTypeValues());

		return out;
	}

	public static MenuItem toServer(final JSONObject jsonMenu) throws JSONException {
		final MenuItem item = singleToServer(jsonMenu);
		if (jsonMenu.has(CHILDREN)) {
			final JSONArray children = jsonMenu.getJSONArray(CHILDREN);
			for (int i = 0; i < children.length(); ++i) {
				final JSONObject child = (JSONObject) children.get(i);
				item.addChild(toServer(child));
			}
		}
		return item;
	}

	private static MenuItem singleToServer(final JSONObject jsonMenu) throws JSONException {
		final MenuItem item = new MenuItemDTO();
		final MenuItemType type = MenuItemType.getType(jsonMenu.getString(TYPE));
		item.setType(type);

		if (!MenuItemType.ROOT.equals(type)) {
			item.setDescription(jsonMenu.getString(DESCRIPTION));
			item.setIndex(jsonMenu.getInt(INDEX));
			item.setReferedClassName(jsonMenu.getString(CLASS_NAME));
			item.setReferencedElementId(getElementId(jsonMenu));
			item.setUniqueIdentifier(defaultIfBlank(jsonMenu.getString(UUID), java.util.UUID.randomUUID().toString()));
		}

		return item;
	}

	private static Integer getElementId(final JSONObject jsonMenu) throws JSONException {
		Integer elementId = null;
		if (jsonMenu.has(ELEMENT_ID)) {
			final String stringElementId = (String) jsonMenu.get(ELEMENT_ID);
			if (notEmpty(stringElementId)) {
				elementId = Integer.valueOf(stringElementId);
			}
		}

		return elementId;
	}

	private static boolean notEmpty(final String s) {
		return !"".equals(s);
	}
}
