(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.configuration.Gis', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CENTER_LATITUDE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.constants.Proxy.CENTER_LONGITUDE, type: 'int', defaultValue: 0 },
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean', defaultValue: false },
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
				data[CMDBuild.core.constants.Proxy.ZOOM_INITIAL_LEVEL] = data[CMDBuild.core.constants.Proxy.INITIAL_ZOOM_LEVEL];

				return data;
			},

			/**
			 * Static function to convert from model's object to legacy one
			 *
			 * @returns {Object}
			 */
			convertToLegacy: function (data) {
				return {
					'center.lat': data[CMDBuild.core.constants.Proxy.CENTER_LATITUDE],
					'center.lon': data[CMDBuild.core.constants.Proxy.CENTER_LONGITUDE],
					enabled: data[CMDBuild.core.constants.Proxy.ENABLED],
					initialZoomLevel: data[CMDBuild.core.constants.Proxy.ZOOM_INITIAL_LEVEL]
				};
			}
		}
	});

})();
