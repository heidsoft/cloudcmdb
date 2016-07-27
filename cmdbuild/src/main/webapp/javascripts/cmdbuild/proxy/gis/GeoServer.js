(function () {

	Ext.define('CMDBuild.proxy.gis.GeoServer', {

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.model.gis.geoServer.BindClass',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		createLayer: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.gis.geoServer.layer.create });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store} store
		 */
		getStore: function () {
			var store =  CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.GIS, {
				model: 'GISLayerModel',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.gis.geoServer.layer.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.LAYERS
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.INDEX, direction: 'ASC' }
				]
			});

			var reload = function(o) {
				if (o) {
					this.nameToSelect = o.nameToSelect;
				}
				this.reload();
			};

			_CMEventBus.subscribe('cmdb-modified-geoserverlayers', reload, store);

			return store;
		},

		/**
		 * Creates store with Classes, Processes andDashboards
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreCardBind: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.gis.geoServer.BindClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function (record) { // Filters root of all classes
						return record.get(CMDBuild.core.constants.Proxy.NAME) != CMDBuild.core.constants.Global.getRootNameClasses();
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				],

				listeners: {
					load: function (store, records, successful, eOpts) { // Add Dashboards items
						CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, {
							url: CMDBuild.proxy.index.Json.dashboard.readAll,
							scope: this,
							success: function (response, options, decodedResponse) {
								decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.DASHBOARDS];

								if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse))
									Ext.Object.each(decodedResponse, function (id, dashboardObject, myself) {
										if (!Ext.Object.isEmpty(dashboardObject)) {
											dashboardObject[CMDBuild.core.constants.Proxy.ID] = id;
											dashboardObject[CMDBuild.core.constants.Proxy.TEXT] = dashboardObject[CMDBuild.core.constants.Proxy.DESCRIPTION];

											store.add(dashboardObject);
										}
									}, this);
							}
						});
					}
				}
			});
		},

		/**
		 * Remove layer
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.gis.geoServer.layer.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.GIS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		updateLayer: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.gis.geoServer.layer.update });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
