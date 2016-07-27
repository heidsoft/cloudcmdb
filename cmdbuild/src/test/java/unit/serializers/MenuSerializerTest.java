package unit.serializers;

import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.services.store.DBDashboardStore;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.services.store.menu.MenuItemDTO;
import org.cmdbuild.services.store.menu.MenuItemType;
import org.cmdbuild.servlets.json.serializers.MenuSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class MenuSerializerTest {

	// @Test
	// public void testToClient() {
	// fail("Not yet implemented");
	// }

	/*
	 * Expected node
	 * 
	 * { index: "", id: "", type: "", description: "", referencedClassName: "",
	 * referencedElementId: "", children: [] }
	 */
	@Test
	public void testRootOnlyToSarver() throws JSONException {
		final MenuItem menuItem = MenuSerializer.toServer(rootJsonMenuItem());
		assertEquals(MenuItemType.ROOT, menuItem.getType());
		assertEquals(0, menuItem.getChildren().size());
	}

	@Test
	public void testWithAClass() throws JSONException {
		final JSONObject jsonRoot = rootJsonMenuItem();
		final JSONObject jsonClassNode = jsonClassNode();
		jsonRoot.put(MenuSerializer.CHILDREN, new JSONArray() {
			{
				put(jsonClassNode);
			}
		});

		final MenuItem root = MenuSerializer.toServer(jsonRoot);
		final MenuItem classMenuItem = root.getChildren().get(0);
		assertEquals(MenuItemType.CLASS, classMenuItem.getType());
		assertEquals(0, classMenuItem.getChildren().size());
		assertEquals(1, classMenuItem.getIndex());
		assertEquals("The class description", classMenuItem.getDescription());
		assertEquals(new Integer(25), classMenuItem.getReferencedElementId());
		assertEquals("FooClass", classMenuItem.getReferedClassName());
	}

	@Test
	public void testRootOnlyToClient() throws JSONException {
		// given
		final TranslationFacade facade = mock(TranslationFacade.class);
		when(facade.read(any(TranslationObject.class))).thenReturn(null);
		final MenuItem menuItem = new MenuItemDTO();
		menuItem.setType(MenuItemType.ROOT);
		final MenuSerializer menuSerializer = MenuSerializer.newInstance() //
				.withRootItem(menuItem) //
				.withTranslationFacade(facade) //
				.build();

		// when
		final JSONObject jsonMenu = menuSerializer.toClient(false);

		// then
		assertEquals(MenuItemType.ROOT.getValue(), jsonMenu.getString(MenuSerializer.TYPE));
		assertEquals(0, jsonMenu.getInt(MenuSerializer.INDEX));
		assertFalse(jsonMenu.has(DESCRIPTION));
		assertFalse(jsonMenu.has(MenuSerializer.CLASS_NAME));
	}

	@Test
	public void testWithAClassToClient() throws JSONException {
		// given
		final TranslationFacade facade = mock(TranslationFacade.class);
		when(facade.read(any(TranslationObject.class))).thenReturn(null);

		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);

		final MenuItemDTO classMenuItem = new MenuItemDTO();
		classMenuItem.setType(MenuItemType.CLASS);
		classMenuItem.setDescription("FooDescription");
		classMenuItem.setReferedClassName("FooClassName");
		classMenuItem.setIndex(1);

		root.addChild(classMenuItem);

		final MenuSerializer menuSerializer = MenuSerializer.newInstance() //
				.withRootItem(root) //
				.withTranslationFacade(facade) //
				.build();

		// when
		final JSONObject jsonMenu = menuSerializer.toClient(false);
		final JSONArray children = jsonMenu.getJSONArray(MenuSerializer.CHILDREN);
		final JSONObject jsonClass = children.getJSONObject(0);

		// then
		assertEquals(MenuItemType.CLASS.getValue(), jsonClass.getString(MenuSerializer.TYPE));
		assertEquals(1, jsonClass.getInt(MenuSerializer.INDEX));
		assertEquals("FooDescription", jsonClass.get(DESCRIPTION));
		assertEquals("FooClassName", jsonClass.get(MenuSerializer.CLASS_NAME));
	}

	@Test
	public void testWithAFolderToClient() throws JSONException {
		// given
		final TranslationFacade facade = mock(TranslationFacade.class);
		when(facade.read(any(TranslationObject.class))).thenReturn(null);
		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);

		final MenuItem folder = new MenuItemDTO();
		folder.setType(MenuItemType.FOLDER);
		folder.setDescription("FooFolderDescription");
		folder.setIndex(1);
		root.addChild(folder);

		final MenuItemDTO classMenuItem = new MenuItemDTO();
		classMenuItem.setType(MenuItemType.CLASS);
		classMenuItem.setDescription("FooDescription");
		classMenuItem.setReferedClassName("FooClassName");
		classMenuItem.setIndex(2);
		folder.addChild(classMenuItem);

		final MenuSerializer menuSerializer = MenuSerializer.newInstance() //
				.withRootItem(root) //
				.withTranslationFacade(facade) //
				.build();

		// when
		final JSONObject jsonMenu = menuSerializer.toClient(false);
		final JSONArray children = jsonMenu.getJSONArray(MenuSerializer.CHILDREN);
		final JSONObject jsonFolder = children.getJSONObject(0);

		// then
		assertEquals(MenuItemType.FOLDER.getValue(), jsonFolder.getString(MenuSerializer.TYPE));
		assertEquals(1, jsonFolder.getInt(MenuSerializer.INDEX));
		assertEquals("FooFolderDescription", jsonFolder.get(DESCRIPTION));
		assertEquals(1, jsonFolder.getJSONArray(MenuSerializer.CHILDREN).length());
		assertFalse(jsonFolder.has(MenuSerializer.CLASS_NAME));

		final JSONObject jsonClass = jsonFolder.getJSONArray(MenuSerializer.CHILDREN).getJSONObject(0);
		assertEquals(MenuItemType.CLASS.getValue(), jsonClass.getString(MenuSerializer.TYPE));
		assertEquals(2, jsonClass.getInt(MenuSerializer.INDEX));
		assertEquals("FooDescription", jsonClass.get(DESCRIPTION));
		assertEquals("FooClassName", jsonClass.get(MenuSerializer.CLASS_NAME));
	}

	@Test
	public void testWithADashboardToClient() throws JSONException {
		final TranslationFacade facade = mock(TranslationFacade.class);
		when(facade.read(any(TranslationObject.class))).thenReturn(null);
		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);

		final MenuItemDTO dashboardMenuItem = new MenuItemDTO();
		dashboardMenuItem.setType(MenuItemType.DASHBOARD);
		dashboardMenuItem.setDescription("FooDashboardDescription");
		dashboardMenuItem.setReferedClassName(DBDashboardStore.DASHBOARD_TABLE);
		dashboardMenuItem.setReferencedElementId(new Integer(15));
		dashboardMenuItem.setIndex(1);

		root.addChild(dashboardMenuItem);

		final MenuSerializer menuSerializer = MenuSerializer.newInstance() //
				.withRootItem(root) //
				.withTranslationFacade(facade) //
				.build();
		final JSONObject jsonMenu = menuSerializer.toClient(false);
		final JSONArray children = jsonMenu.getJSONArray(MenuSerializer.CHILDREN);
		final JSONObject jsonDashboard = children.getJSONObject(0);

		assertEquals(MenuItemType.DASHBOARD.getValue(), jsonDashboard.getString(MenuSerializer.TYPE));
		assertEquals(1, jsonDashboard.getInt(MenuSerializer.INDEX));
		assertEquals("FooDashboardDescription", jsonDashboard.get(DESCRIPTION));
		assertEquals(DBDashboardStore.DASHBOARD_TABLE, jsonDashboard.get(MenuSerializer.CLASS_NAME));
		assertEquals(15, jsonDashboard.getLong(MenuSerializer.ELEMENT_ID));
	}

	// TODO test report to Client

	private JSONObject rootJsonMenuItem() throws JSONException {
		final JSONObject jsonRoot = new JSONObject();
		jsonRoot.put(MenuSerializer.TYPE, "root");

		return jsonRoot;
	}

	private JSONObject jsonClassNode() throws JSONException {
		final JSONObject jsonClassNode = new JSONObject();
		jsonClassNode.put(MenuSerializer.TYPE, "class");
		jsonClassNode.put(DESCRIPTION, "The class description");
		jsonClassNode.put(MenuSerializer.INDEX, "1");
		jsonClassNode.put(MenuSerializer.ELEMENT_ID, "25");
		jsonClassNode.put(MenuSerializer.CLASS_NAME, "FooClass");
		jsonClassNode.put(MenuSerializer.UUID, "uuid");

		return jsonClassNode;
	}
}
