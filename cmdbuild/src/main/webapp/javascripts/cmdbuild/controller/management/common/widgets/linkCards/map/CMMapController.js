(function() {

	Ext.define("CMDBuild.controller.management.common.widgets.linkCards.map.CMMapController", {

		requires: [
			'CMDBuild.controller.management.common.widgets.linkCards.map.LayerBuilder',
			'CMDBuild.proxy.gis.Gis',
			'CMDBuild.view.management.common.widgets.linkCards.map.CMMiniCardGrid'
		],

		mixins: {
			observable: "Ext.util.Observable",
			mapDelegate: "CMDBuild.view.management.common.widgets.linkCards.map.CMMapPanelDelegate",
			editingWindowDelegate: "CMDBuild.controller.management.common.widgets.linkCards.map.CMMapEditingWindowDelegate",
			layerSwitcherDelegate: "CMDBuild.view.management.common.widgets.linkCards.map.CMMapLayerSwitcherDelegate",
			cardStateDelegate: "CMDBuild.state.CMCardModuleStateDelegate",
			miniCardGridDelegate: "CMDBuild.view.management.common.widgets.linkCards.map.CMMiniCardGridDelegate"
		},

		cardDataName: "geoAttributes", // CMCardDataProvider member, to say the name to use for given data

		updateMap: function(entryType) {
			var me = this;
			// at first clear the panel calling the updateMap method;
			this.mapPanel.updateMap(entryType);
			// then do something build new layers
			_CMCache.getVisibleLayersForEntryTypeName(entryType.get("name"), function(layers) {
				var orderedAttrs = sortAttributesByIndex(layers);
				me.mapState.update(orderedAttrs, me.map.getZoom());
				me.map.activateStrategies(true);
				/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/
				var layers = me.map.layers;
				var layersPanel = me.mapPanel.getLayerSwitcherPanel();
//				for (var i=0, l=layers.length; i<l; ++i) {
//					if (/*layers[i].visibility === undefined && */layers[i].CM_geoserverLayer && layers[i].geoAttribute !== undefined) {
//						var checked = getLayerVisibility(me.currentCardId, layers[i].geoAttribute.cardBinding, layers[i].geoAttribute.visibility);
//						layersPanel.setItemCheckByLayerId(layers[i].id, checked);
//						layers[i].visibility = undefined;
//						layers[i].setVisibility(checked);
//					}
//				}

			});
		},

		onAddCardButtonClick: function() {
			this.mapPanel.getMap().clearSelection();
			this.currentCardId = undefined;
			this.mapPanel.getMap().refreshStrategies();
		},

		centerMapOnFeature: function(params) {
			if (params == null) {
				return;
			}

			var me = this;

			function onSuccess(resp, req, feature) {
				// the card could have no feature
				if (feature.properties) {
					me.mapPanel.getMap().centerOnGeometry(feature);
				} else {
					me.mapPanel.getMap().clearSelection();
				}
			};

			var entryTypeId = params.IdClass;
			var entryType = _CMCache.getEntryTypeById(entryTypeId);
			_CMCache.getLayersForEntryTypeName(entryType.getName(), function(layers) {
				if (layers.length > 0) {
					var layer = layers[0];
					if (me.map.getZoom() < layer.minZoom ) {
						// change the zoom to the minimum to show the feature
						me.map.setCenter(me.map.getCenter(), layer.minZoom);
					}
					CMDBuild.proxy.gis.Gis.getFeature({
						params: {
							"className": _CMCache.getEntryTypeNameById(params.IdClass),
							"cardId": params.Id
						},
						loadMask: false,
						scope: this,
						success: onSuccess
					});
				}
			});

		},

		editMode: function() {
			this.cmIsInEditing = true;

			if (this.mapPanel.cmVisible) {
				this.mapPanel.editMode();
				this.deactivateSelectControl();
			}
		},

		displayMode: function() {
			this.cmIsInEditing = false;

			if (this.mapPanel.cmVisible) {
				this.mapPanel.displayMode();
				this.editingWindowDelegate.deactivateEditControls();
				this.activateSelectControl();
			}
		},

		onCardSaved: function(c) {
			/*
			 * Normally after the save, the main controller say to the grid to reload it, and select the new card. If the map is
			 * visible on save, this could not be done, so say to this controller to refresh the features loaded, and set the new card
			 * as selected
			 */
			if (this.mapPanel.cmVisible) {
				var me = this;

				_CMCardModuleState.setCard({
					Id: c.Id,
					IdClass: c.IdClass
				}, function(card) {
					me.mapPanel.getMap().clearSelection();
					me.mapPanel.getMap().refreshStrategies();
				});
			}
		},

		deactivateSelectControl: function() {
			this.selectControl.deactivate();
		},

		activateSelectControl: function() {
			this.selectControl.activate();
		},

		selectFeature: function(feauture) {
			this.selectControl.select(feauture);
		},

		onEntryTypeSelected: onEntryTypeSelected,
		getCardData: getCardData,
		getCardDataName: function() {
			return this.cardDataName;
		},

		/* As mapDelegate *********/

		onLayerAdded: onLayerAdded,
		onLayerRemoved: onLayerRemoved,
		onMapPanelVisibilityChanged: onVisibilityChanged,

		/* As layerSwitcherDelegate *********/

		onLayerCheckChange: function(node, checked) {
			var map = this.mapPanel.getMap();
			if (map) {
				var layer = map.getLayersBy("id", node.layerId);
				if (layer.length > 0) {
					layer[0].setVisibility(checked);
				}
			}
		},

		/* As CMCardModuleStateDelegate ***************/

		onEntryTypeDidChange: function(state, entryType, danglingCard) {
			this.onEntryTypeSelected(entryType, danglingCard);
		},

		onCardDidChange: function(state, card) {
			this.onCardSelected(card);
		},

		/* As CMMap delegate ****************/

		featureWasAdded: function(feature) {
			if (feature.data) {
				var data = feature.data;
				var currentClassId = _CMCardModuleState.entryType ? _CMCardModuleState.entryType.getId() : null;
				var currentCardId = _CMCardModuleState.card ? _CMCardModuleState.card.get("Id") : null;

				if (data.master_card == currentCardId
						&& data.master_class == currentClassId) {

					feature.layer.selectFeature(feature);
				}
			}
		},

		/* As miniCardGridDelegate ************/

		miniCardGridDidActivate: loadMiniCardGridStore,
		miniCardGridWantOpenCard: function(grid, card) {
			_CMCardModuleState.setCard(card);
		},

		// As CMDBuild.state.CMMapStateDelegate

		geoAttributeUsageChanged: function(geoAttribute) {
			if (!geoAttribute.isUsed()) {
				removeLayerForGeoAttribute(this.map, geoAttribute, this);
			} else {
				addLayerForGeoAttribute(this.map, geoAttribute, this);
			}
		},

		geoAttributeZoomValidityChanged: function(geoAttribute) {
			if (!geoAttribute.isZoomValid()) {
				removeLayerForGeoAttribute(this.map, geoAttribute, this);
			} else {
				addLayerForGeoAttribute(this.map, geoAttribute, this);
			}
		},

		featureVisibilityChanged: function(className, cardId, visible) {
			var layers = this.map.getLayersByTargetClassName(className);

			for (var i=0, layer=null; i<layers.length; ++i) {
				layer = layers[i];
				if (visible) {
					if (typeof layer.showFeatureWithCardId == "function") {
						layer.showFeatureWithCardId(cardId);
					}
				} else {
					if (typeof layer.hideFeatureWithCardId == "function") {
						layer.hideFeatureWithCardId(cardId);
					}
				}
			}
		},

		getCurrentCardId: function() {
			return this.currentCardId;
		},

		getCurrentMap: function() {
			return this.map;
		}
	});

	function getLayerVisibility(id, bindings, visibles) {
		for (var i = 0; i < bindings.length; i++) {
			if (Ext.Array.contains(visibles, bindings[i].className)) {
				if (bindings[i].idCard == id) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Executed after zoomEvent to update mapState object and manually redraw all map's layers
	 */
	function onZoomEnd() {
		var map = this.map;
		var zoom = map.getZoom();
		this.mapState.updateForZoom(zoom);
		var baseLayers = map.cmBaseLayers;
		var haveABaseLayer = false;

		// Manually force redraw of all layers to fix a problem with GoogleMaps
		Ext.Array.each(map.layers, function(item, index, allItems) {
			item.redraw();
		});

		for (var i = 0; i < baseLayers.length; ++i) {
			var layer = baseLayers[i];

			if (!layer || typeof layer.isInZoomRange != 'function')
				continue;

			if (layer.isInZoomRange(zoom)) {
				map.setBaseLayer(layer);
				haveABaseLayer = true;

				break;
			}
		}

		if (!haveABaseLayer)
			map.setBaseLayer(map.cmFakeBaseLayer);
	}

	function buildLongPressController(me) {
		var map = me.map;
		var longPressControl = new OpenLayers.Control.LongPress({
			onLongPress: function(e) {
				var lonlat = map.getLonLatFromPixel(e.xy);
				var features = map.getFeaturesInLonLat(lonlat);

				// no features no window
				if (features.length == 0) {
					return;
				}

				me.miniCardGridWindowController.setFeatures(features);
				if (me.miniCardGridWindow) {
					me.miniCardGridWindow.close();
				}

				me.miniCardGridWindow = Ext.create('CMDBuild.view.management.common.widgets.linkCards.map.CMMiniCardGridWindow', {
					width: me.mapPanel.getWidth() / 100 * 40,
					height: me.mapPanel.getHeight() / 100 * 80,
					x: e.xy.x,
					y: e.xy.y,
					dataSource: me.miniCardGridWindowController.getDataSource()
				});

				me.miniCardGridWindowController.bindMiniCardGridWindow(me.miniCardGridWindow);
				me.miniCardGridWindow.show();
			}
		});

		map.addControl(longPressControl);
		longPressControl.activate();
	}

	function loadMiniCardGridStore(grid) {
		if (!grid.isVisible()) {
			return;
		}

		var ds = grid.getDataSource();
		var currentIdClass = _CMCardModuleState.entryType.getId();

		if (!ds || ds.getLastEntryTypeIdLoaded() == currentIdClass) {
			return;
		}

		ds.loadStoreForEntryTypeId(currentIdClass, //
			function(records, operation, success) {
				var currentCard = _CMCardModuleState.card;
				if (!currentCard) {
					return;
				}

				for (var i=0, r=null; i<records.length; ++i) {
					r = records[i];
					if (r && r.get("Id") == currentCard.get("Id")
							&& r.get("IdClass") == currentCard.get("IdClass")) {

						grid.selectRecordSilently(r);
					}
				}
			}
		);
	}

	function updateMiniCardGridTitle(entryType, grid) {
		var prefix = CMDBuild.Translation.management.modcard.title;
		grid.setTitle(prefix + entryType.get("name"));
	}

	function getCardData() {
		return Ext.JSON.encode(this.mapPanel.getMap().getEditedGeometries());
	}

	function onEntryTypeSelected(et, danglingCard) {
		if (!et || !this.mapPanel.cmVisible) {
			return;
		}

		var newEntryTypeId = et.get("id");
		if (this.currentClassId != newEntryTypeId) {
			this.currentClassId = newEntryTypeId;
			this.updateMap(et);

			var miniCardGrid = this.mapPanel.getMiniCardGrid();
			loadMiniCardGridStore(miniCardGrid);
			updateMiniCardGridTitle(et, miniCardGrid);
		}

		if (danglingCard) {
			_CMCardModuleState.setCard(danglingCard);
		} else {
			// check for card selected when update
			// the map on show
			var lastCard = _CMCardModuleState.card;
			if (lastCard) {
				this.onCardSelected(lastCard);
			} else {
				this.currentCardId = undefined;
			}
		}
	}

	function onLayerAdded(map, params) {
		var layer = params.layer;
		var me = this;

		if (layer == null) {
			return;
		}

		if (layer.CM_geoserverLayer) {
			layer.setVisibility(this.mapState.isGeoServerLayerVisible(layer.name));
		}

		if (layer.CM_Layer) {
			this.editingWindowDelegate.buildEditControls(layer);
			this.selectControl.addLayer(layer);

			layer.events.on({
				"beforefeatureadded": beforefeatureadded,
				"scope": me
			});
		}
	}

	function onLayerRemoved(map, params) {
		var layer = params.layer;
		var me = this;

		if (layer == null || !layer.CM_Layer) {
			return;
		}

		this.editingWindowDelegate.destroyEditControls(layer);
		this.selectControl.removeLayer(layer);

		layer.events.un({
			"beforefeatureadded": beforefeatureadded,
			"scope": me
		});
	}

	function beforefeatureadded(o) {
		var layer = o.object;

		if (layer.CM_EditLayer) {
			if (layer.features.length > 0) {
				var currentFeature = layer.features[0];
				if (o.feature.attributes.master_card) {
					// add a feature in edit layer
					// because was selected by the user
					if (currentFeature.attributes.master_card == o.feature.attributes.master_card) {
						return false; // forbid the add
					} else {
						layer.removeFeatures([currentFeature]);
					}
				} else {
					// is added in editing mode
					// and want only one feature
					layer.removeAllFeatures();
					return true;
				}
			}
		} else {
			var data = o.feature.data;

			if (CMDBuild.configuration.gis.get('cardBrowserByDomainConfiguration')['root']) { // TODO: use proxy constants
				if (!this.mapState.isFeatureVisible(data.master_className, data.master_card)) { // could be also null, or undefined
					layer.hideFeatureWithCardId(data.master_card, o.feature);
					return false;
				}
			}
		}

		return true;
	}

	function onCmdbLayerBeforeAdd(o) {
		var layer = o.object,
			feature = o.feature;

		if (this.currentCardId
				&& this.currentCardId == feature.data.master_card) {

			layer.selectFeature(feature);
		}
	}

	function onVisibilityChanged(map, visible) {
		if (visible) {
			var lastClass = _CMCardModuleState.entryType,
				lastCard = _CMCardModuleState.card;

			if (lastClass
				&& this.currentClassId != lastClass.get("id")) {

				this.onEntryTypeSelected(lastClass);
			} else {
				if (lastCard
						&& (!this.currentCardId || this.currentCardId != lastCard.get("Id"))) {

					this.centerMapOnFeature(lastCard.data);
					this.onCardSelected(lastCard);
				}
			}

			if (this.cmIsInEditing) {
				this.editMode();
			} else {
				this.displayMode();
			}
		} else {
			if (this.cmIsInEditing) {
				this.mapPanel.displayMode();
			}
		}
	}

	//////***************************************************************** /////

	function removeLayerForGeoAttribute(map, geoAttribute, me) {
		var l = getLayerByGeoAttribute(map, geoAttribute);
		if (l) {
			if (!geoAttribute.isUsed()
					&& l.editLayer) {

				map.removeLayer(l.editLayer);
			}

			l.events.unregister("visibilitychanged", me, onLayerVisibilityChange);
			l.destroyStrategies();
			l.clearSelection();

			map.removeLayer(l);
		}
	}

	function addLayerForGeoAttribute(map, geoAttribute, me) {
		if (!geoAttribute.isZoomValid()) {
			return;
		}

		addLayerToMap(map,
			CMDBuild.controller.management.common.widgets.linkCards.map.LayerBuilder.buildLayer({
				classId : _CMCardModuleState.entryType.get("id"),
				geoAttribute : geoAttribute.getValues(),
				withEditLayer : true
			}, map),
			me
		);
	}

	function addLayerToMap(map, layer, me) {
		if (layer) {
			layer.events.register("visibilitychanged", me, onLayerVisibilityChange);
			me.mapState.addLayer(layer, map.getZoom());
			map.addLayer(layer);

			if (typeof layer.cmdb_index != "undefined") {
				map.setLayerIndex(layer, layer.cmdb_index);
			}

			if (layer.editLayer) {
				var el = map.getLayerByName(layer.editLayer.name);
				if (!el) {
					map.addLayers([layer.editLayer]);
				}
			}
		}
	};

	function getLayerByGeoAttribute(me, geoAttribute) {
		for (var i=0, l=me.layers.length; i<l; ++i) {
			var layer = me.layers[i];
			if (!layer.geoAttribute || layer.CM_EditLayer) {
				continue;
			} else if (CMDBuild.state.GeoAttributeState.getKey(layer.geoAttribute) == geoAttribute.getKey()) {
				return layer;
			}
		}
		return null;
	}

	function onLayerVisibilityChange(param) {
		var layer = param.object;
		this.mapState.updateLayerVisibility(layer, this.map.getZoom());

		var cardBrowserPanel = this.mapPanel.getCardBrowserPanel();
		if (layer.CM_geoserverLayer && cardBrowserPanel) {
			cardBrowserPanel.udpateCheckForLayer(layer);
		}
	};

	function sortAttributesByIndex(geoAttributes) {
		var cmdbuildLayers = [];
		var geoserverLayers = [];
		for (var i=0, l=geoAttributes.length; i<l; ++i) {
			var attr = geoAttributes[i];
			if (attr.masterTableId) {
				cmdbuildLayers[attr.index] = attr;
			} else {
				geoserverLayers[attr.index] = attr;
			}
		}

		return cmdbuildLayers.concat(geoserverLayers);
	}

})();
