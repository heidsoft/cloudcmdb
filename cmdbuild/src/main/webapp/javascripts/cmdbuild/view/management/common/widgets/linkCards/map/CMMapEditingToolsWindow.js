(function() {

	var ICON_CLS = {
		"POINT": "mapFeaturePoint",
		"LINESTRING": "mapFeatureLine",
		"POLYGON": "mapFeaturePolygon"
	};

	/**
	 * This window looks like an external toolbar (Photoshop style)
	 * After the creation is possible to say to the window to show the tools
	 * for a specific geometry type
	 *
	 * The window can be shown only if the owner panel is visible
	 *
	 * @link CMDBuild.view.management.map.CMMapEditingToolsWindow
	 */
	Ext.define("CMDBuild.view.management.common.widgets.linkCards.map.CMMapEditingToolsWindow", {
		extend: "Ext.Window",

		requires: ['CMDBuild.view.management.common.widgets.linkCards.map.CMMapEditingToolsWindowDelegate'],

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		editingControls: {},
		layers: {},

		owner: {
			getPosition: function() { return [0,0]; }
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this, "CMDBuild.view.management.common.widgets.linkCards.map.CMMapEditingToolsWindowDelegate");
			this.callParent(arguments);
		},

		initComponent: function() {

			this.closable = false;
			this.hideBorders = true;
			this.resizable = false;
			this.frame = false;

			this.layout = {
				type: 'hbox',
				padding: '2',
				align: 'stretch'
			};

			this.geoAttrMenuButton = new Ext.Button({
				text: CMDBuild.Translation.management.modcard.gis.geo_attributes,
				menu: new Ext.menu.Menu({
					items: []
				})
			});

			this.addButton = new Ext.Button({
				text: CMDBuild.Translation.add,
				creationControl: undefined,
				iconCls: 'add',
				enableToggle: true,
				allowDepress: true,
				disabled: true,
				scope: this,
				toggleHandler: function(button, state) {
					this.callDelegates("addFeatureButtonHasBeenToggled", state);
				}
			});

			this.removeButton = new Ext.Button({
				text: CMDBuild.Translation.remove,
				iconCls: 'delete',
				scope: this,
				disabled: true,
				handler: function() {
					this.callDelegates("removeFeatureButtonHasBeenClicked");
				}
			});

			this.callParent(arguments);
		},

		// add the buttons on render
		// to allow the window to resize it
		// automatically
		onRender: function() {
			this.callParent(arguments);

			this.add([
				this.geoAttrMenuButton,
				this.addButton,
				this.removeButton
			]);
		},

		show: function() {
			if (this.geoAttrMenuButton.menu.items.length > 0) {

				this.callParent(arguments);

				var firstItemOfMenu = this.geoAttrMenuButton.menu.items.first();
				if (firstItemOfMenu) {
					onAddMenuitemSelect.call(this, firstItemOfMenu);
				}

			}
		},

		addLayer: function(layer) {
			if (layer) {
				if (!this.layers[layer.name]) {
					this.layers[layer.name] = this.geoAttrMenuButton.menu.add({
						iconCls: ICON_CLS[layer.geoAttribute.type],
						text: layer.geoAttribute.description,
						editLayer: layer.editLayer,
						geoType: layer.geoAttribute.type,
						scope: this,
						handler: onAddMenuitemSelect
					});
				}
			}
		},

		removeAllLayerBinding: function() {
			this.geoAttrMenuButton.menu.removeAll(true);
			this.layers = {};
		}
	});

	function onAddMenuitemSelect(item) {
		this.geoAttrMenuButton.setText(item.text);
		this.geoAttrMenuButton.setIconCls(item.iconCls);
		this.addButton.enable();
		this.addButton.toggle(false);
		this.removeButton.enable();

		this.callDelegates("geoAttributeMenuItemHasBeenClicked", item.editLayer);
	};

})();