(function() {
	var tr = CMDBuild.Translation.administration.modcartography.geoserver;

	Ext.require('CMDBuild.proxy.gis.GeoServer');

	Ext.define("CMDBuild.controller.administration.gis.CMModGeoServerController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);

			this.view.on("show", function() {
				this.view.layersGrid.onModShow(this.view.firstShow);
				this.view.firstShow = false;
			}, this);

			this.view.layersGrid.getSelectionModel().on("selectionchange", onLayerSelect, this);

			this.view.addLayerButton.on("click", onAddButtonClick, this);

			this.view.form.saveButton.on("click", onSaveButtonClick, this);
			this.view.form.abortButton.on("click", onAbortButtonClick, this);
			this.view.form.deleteButton.on("click", onDeleteButtonClick, this);
		},

		onViewOnFront: function() {
			if (!geoserverIsEnabled()) {
				var msg = Ext.String.format(tr.service_not_available
						, CMDBuild.Translation.administration.modcartography.title +
							"/" + CMDBuild.Translation.administration.modcartography.external_services.title);

				CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleShow', {
					identifier: "notconfiguredpanel",
					parameters: msg
				});
				return false;
			}

			this.view.layersGrid.selectFirstIfUnselected();
		}
	});

	function onAddButtonClick() {
		this.lastSelection = null;
		this.view.form.onAddLayer();
		this.view.layersGrid.clearSelection();
	}

	function onLayerSelect(view, selection) {
		if (selection[0]) {
			this.lastSelection = selection[0];
			this.view.form.onLayerSelect(this.lastSelection);
		}
	}

	function onSaveButtonClick() {
		var nameToSelect = this.view.form.getName();
		var cardBinding = this.view.form.getCardsBinding();

		if (this.view.form.isValid()) {
			if (this.lastSelection) {
				CMDBuild.proxy.gis.GeoServer.updateLayer({
					form: this.view.form.getForm(),
					params: {
						name: nameToSelect,
						cardBinding: Ext.encode(cardBinding)
					},
					scope: this,
					success: function() {
						_CMCache.onGeoAttributeSaved();
						this.view.form.disableModify();
						this.view.layersGrid.loadStoreAndSelectLayerWithName(nameToSelect);
					},
					failure: function() {
						_debug("Failed to add or modify a Geoserver Layer", arguments);
					},
					callback: function() {
						CMDBuild.core.LoadMask.hide();
					}
				});
			} else {
				CMDBuild.proxy.gis.GeoServer.createLayer({
					form: this.view.form.getForm(),
					params: {
						name: nameToSelect,
						cardBinding: Ext.encode(cardBinding)
					},
					scope: this,
					success: function() {
						_CMCache.onGeoAttributeSaved();
						this.view.form.disableModify();
						this.view.layersGrid.loadStoreAndSelectLayerWithName(nameToSelect);
					},
					failure: function() {
						_debug("Failed to add or modify a Geoserver Layer", arguments);
					},
					callback: function() {
						CMDBuild.core.LoadMask.hide();
					}
				});
			}
		}
	};

	function onAbortButtonClick() {
		if (this.lastSelection) {
			this.view.form.onLayerSelect(this.lastSelection);
		} else {
			this.view.form.disableModify();
			this.view.form.reset();
		}
	}

	function onDeleteButtonClick() {
		var me = this;
		Ext.Msg.show({
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					CMDBuild.core.LoadMask.show();
					var layerName = me.view.form.getName();
					CMDBuild.proxy.gis.GeoServer.remove({
						params: {
							name: layerName
						},
						loadMask: false,
						important: true,
						scope: this,
						callback: function() {
							_CMCache.onGeoAttributeDeleted("_Geoserver", layerName);
							me.view.layersGrid.loadStoreAndSelectLayerWithName();
							CMDBuild.core.LoadMask.hide();
						}
					});
				}
			}
		});
	};

	function geoserverIsEnabled() {
		return CMDBuild.configuration.gis.get([CMDBuild.core.constants.Proxy.GEO_SERVER, 'enabled']); // TODO: use proxy constants
	}
})();