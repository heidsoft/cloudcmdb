(function() {

	Ext.require('CMDBuild.proxy.gis.Gis');

	Ext.define("CMDBuild.controller.management.common.widgets.linkCards.map.CMCardBrowserTreeDataSource", {

		GEOSERVER: "GeoServer",

		constructor: function(cardBrowserTree, mapState) {
			this.cardBrowserTree = cardBrowserTree;
			this.cardBrowserTree.setDataSource(this);
			this.mapState = mapState;
			this.configuration = CMDBuild.configuration.gis.get('cardBrowserByDomainConfiguration'); // TODO: use proxy constants
			this.refresh();
			this.callParent(arguments);
		},

		refresh: function() {
			var me = this;
			me.cardBrowserTree.setRootNode({
				loading: true,
				text: CMDBuild.Translation.common.loading
			});

			// fill the first level of tree nodes
			// asking the cards according to the
			// root of the configuration
			CMDBuild.proxy.gis.Gis.expandDomainTree({
				loadMask: false,
				success: function successGetCardBasicInfoList(operation, options, response) {
					addGeoserverLayersToTree(response.root, me);
					me.cardBrowserTree.setRootNode(response.root);
				}
			});
		}
	});

	function addGeoserverLayersToTree(root, me) {
		var children = (root) ? root.children || [] : [];
		for (var i=0, l=children.length; i<l; ++i) {
			addGeoserverLayersToTree(children[i], me);
		}

		addGeoserverLayerIfConfigured(root, me);
	}

	function addGeoserverLayerIfConfigured(nodeConfiguration, me) {
		var mapping = me.configuration.geoServerLayersMapping;
		if (mapping) {
			var layerPerClass = mapping[nodeConfiguration.className];
			if (layerPerClass) {
				// TODO: More than one GeoServer layer per card
				var layerPerCard = layerPerClass[nodeConfiguration.cardId];
				if (layerPerCard) {
					nodeConfiguration.children = [{
						text: layerPerCard.description,
						cardId: layerPerCard.name,
						className: "GeoServer",
						leaf: true,
						// the geoserver layer must be visible only
						// if is visible the binded card node
						checked: nodeConfiguration.checked
					}].concat(nodeConfiguration.children);
				}
			}
		}

		return nodeConfiguration;
	}
})();