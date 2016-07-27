(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.core.configurations.builder.gis.Gis', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CARD_BROWSER_BY_DOMAIN_CONFIGURATION, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.CENTER_LATITUDE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.constants.Proxy.CENTER_LONGITUDE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.GEO_SERVER, type: 'auto' }, // {CMDBuild.model.core.configurations.builder.gis.Geoserver}
			{ name: CMDBuild.core.constants.Proxy.GOOGLE, type: 'auto' }, // {CMDBuild.model.core.configurations.builder.gis.Google}
			{ name: CMDBuild.core.constants.Proxy.OSM, type: 'auto' }, // {CMDBuild.model.core.configurations.builder.gis.Osm}
			{ name: CMDBuild.core.constants.Proxy.YAHOO, type: 'auto' }, // {CMDBuild.model.core.configurations.builder.gis.Yahoo}
			{ name: CMDBuild.core.constants.Proxy.ZOOM_INITIAL_LEVEL, type: 'int', defaultValue: 3 }
		],

		statics: {
			/**
			 * Static function to convert from legacy object to model's one
			 *
			 * @param {Object} data
			 *
			 * @returns {Object} data
			 */
			convertFromLegacy: function (data) {
				data = data || {};
				data[CMDBuild.core.constants.Proxy.CENTER_LATITUDE] = data['center.lat'];
				data[CMDBuild.core.constants.Proxy.CENTER_LONGITUDE] = data['center.lon'];
				data[CMDBuild.core.constants.Proxy.ZOOM_INITIAL_LEVEL] = data['initialZoomLevel'];

				return data;
			}
		},

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (data) {
			data = CMDBuild.model.core.configurations.builder.gis.Gis.convertFromLegacy(data);
			data[CMDBuild.core.constants.Proxy.GEO_SERVER] = Ext.create('CMDBuild.model.core.configurations.builder.gis.Geoserver', Ext.clone(data));
			data[CMDBuild.core.constants.Proxy.GOOGLE] = Ext.create('CMDBuild.model.core.configurations.builder.gis.Google', Ext.clone(data));
			data[CMDBuild.core.constants.Proxy.OSM] = Ext.create('CMDBuild.model.core.configurations.builder.gis.Osm', Ext.clone(data));
			data[CMDBuild.core.constants.Proxy.YAHOO] = Ext.create('CMDBuild.model.core.configurations.builder.gis.Yahoo', Ext.clone(data));

			this.callParent(arguments);
		},

		/**
		 * Override to permits multilevel get with a single function
		 *
		 * @param {Array or String} property
		 *
		 * @returns {Mixed}
		 *
		 * @override
		 */
		get: function (property) {
			if (!Ext.isEmpty(property) && Ext.isArray(property)) {
				var returnValue = this;

				Ext.Array.each(property, function (propertyName, i, allPropertyNames) {
					if (!Ext.isEmpty(returnValue) && Ext.isFunction(returnValue.get))
						returnValue = returnValue.get(propertyName);
				}, this);

				return returnValue;
			}

			return this.callParent(arguments);
		}
	});

})();
