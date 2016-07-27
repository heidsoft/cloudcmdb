(function() {

	Ext.require('CMDBuild.proxy.gis.GeoServer');

	var tr = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_geo = CMDBuild.Translation.administration.modClass.geo_attributes;

	Ext.define('CMDBuild.view.administration.gis.GisLayersGrid', {
		extend: 'Ext.grid.Panel',

		border: false,
		enableDragDrop: true,
		frame: false,
		loadMask: true,

		initComponent: function() {
			this.sm = Ext.create('Ext.selection.RowModel');

			this.gridColumns = [
				{
					header: tr.name,
					hideable: true,
					hidden: false,
					sortable: false,
					dataIndex: CMDBuild.core.constants.Proxy.NAME,
					flex: 1
				},
				{
					header: tr.description,
					hideable: true,
					hidden: false,
					sortable: false,
					dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
					flex: 1
				},
				{
					header: tr.type,
					hideable: true,
					hidden: false,
					sortable: false,
					dataIndex: CMDBuild.core.constants.Proxy.TYPE,
					flex: 1
				},
				{
					header: tr_geo.min_zoom,
					hideable: true,
					hidden: false,
					sortable: false,
					dataIndex: 'minZoom',
					flex: 1
				},
				{
					header: tr_geo.max_zoom,
					hideable: true,
					hidden: false,
					sortable: false,
					dataIndex: 'maxZoom',
					flex: 1
				}
			];

			this.gridStore = CMDBuild.proxy.gis.GeoServer.getStore();

			Ext.apply(this, {
				columns: this.gridColumns,
				store: this.gridStore
			});

			this.callParent(arguments);
		},

		clearSelection: function() {
			this.getSelectionModel().deselectAll();
		},

		/**
		 * @param {Boolean} firstLoad
		 */
		onModShow: function(firstLoad) {
			this.store.load();
		},

		selectFirstIfUnselected: function() {
			var sm = this.getSelectionModel();

			if (!sm.hasSelection()) {
				this.selectFirst();
			}
		},

		/**
		 * @param {Int} attempts
		 */
		selectFirst: function(attempts) {
			var me = this;
			var _attempts = attempts || 10;

			if (this.store.isLoading()) {
				Ext.Function.createDelayed(me.selectFirst, 500, me, [--_attempts])();

				return;
			}

			if (this.store.count() != 0) {
				try {
					var sm = this.getSelectionModel();

					sm.select(0);
				} catch (e) { }
			}
		},

		/**
		 * @param {String} name
		 */
		loadStoreAndSelectLayerWithName: function(name) {
			var me = this;

			this.store.load({
				callback: function(records, operation, success) {
					var toSelect = me.store.find(CMDBuild.core.constants.Proxy.NAME, name);

					if (toSelect >= 0) {
						me.getSelectionModel().select(toSelect);
					} else {
						me.selectFirst();
					}
				}
			});
		}
	});

})();