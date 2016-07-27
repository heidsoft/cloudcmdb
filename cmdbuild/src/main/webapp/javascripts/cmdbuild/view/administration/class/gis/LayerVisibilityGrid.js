(function() {

	Ext.require('CMDBuild.proxy.gis.Layer');

	Ext.define("CMDBuild.Administration.LayerVisibilityGrid", {
		extend: "CMDBuild.Administration.LayerGrid",
		currentClass: undefined,

		cmCheckColumnReadOnly: false,

		initComponent: function() {
			this.callParent(arguments);
			var me = this;

			this.mon(this, "activate", function() {
				CMDBuild.core.LoadMask.show();
				me.store.load({
					callback: function(records, operation, success) {
						selectVisibleLayers.call(me, me.currentClassId);
						CMDBuild.core.LoadMask.hide();
					}
				});
			}, null);
		},

		onClassSelected: function(s) {
			this.currentClassId = s.id || 0;
			selectVisibleLayers.call(this, this.currentClassId);
		},

		/**
		 * @override
		 */
		onVisibilityChecked: function(cell, recordIndex, checked) {
			var record = this.store.getAt(recordIndex);
			var et = _CMCache.getEntryTypeById(this.currentClassId);

			CMDBuild.core.LoadMask.show();
			CMDBuild.proxy.gis.Layer.setVisibility({
				params: {
					tableName: et.get("name"),
					layerFullName: record.getFullName(),
					visible: checked
				},
				important: true,
				loadMask: false,
				success: function() {
					_CMCache.onGeoAttributeVisibilityChanged();
					record.setVisibilityForTableName(et.get("name"), checked);
				},
				failure: function() {
					record.set(column.dataIndex, !checked);
				},
				callback: function() {
					CMDBuild.core.LoadMask.hide();
					record.commit();
				}
			});
		}
	});

	function selectVisibleLayers(tableId) {
		Ext.suspendLayouts();
		var et = _CMCache.getEntryTypeById(tableId);
		var s = this.store;
		var columnDataIndex = this.getVisibilityColDataIndex();

		s.each(function(record) {
			record.set(columnDataIndex, record.isVisibleForEntryType(et));
			record.commit();
		});
		Ext.resumeLayouts();
	};
})();