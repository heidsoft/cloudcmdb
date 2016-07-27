(function() {

	Ext.require('CMDBuild.proxy.gis.Layer');

	Ext.define("CMDBuild.controller.administration.gis.CMModLayerOrderController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.view.mon(this.view, "cm-rowmove", onRowMoved, this);
		},

		onViewOnFront: function() {
			this.view.store.load();
		}
	});

	/*
	 *p = {
		node: node,
		data: data,
		dropRec: dropRec,
		dropPosition: dropPosition
	}*/
	function onRowMoved(p) {
		var oldIndex = getOldIndex(p.data);
		var newIndex = getNewIndex(p.dropRec);
		var me = this;

		CMDBuild.core.LoadMask.show();
		CMDBuild.proxy.gis.Layer.setOrder({
			params: {
				"oldIndex": oldIndex,
				"newIndex": newIndex
			},
			important: true,
			loadMask: false,
			scope: this,
			callback: function() {
				CMDBuild.core.LoadMask.hide();
				_CMCache.onGeoAttributeSaved(); // load always to sync the index
			}
		});
	}

	function getOldIndex(data) {
		var oldIndex = -1;
		try {
			oldIndex = data.records[0].data.index;
		} catch (e) {
			CMDBuild.log.Error("Can not get the old index");
		}

		return oldIndex;
	}

	function getNewIndex(dropRec, dropPosition) {
		return dropRec.data.index;
	}
})();