(function () {

	Ext.define('CMDBuild.core.configurations.builder.Gis', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.core.configurations.builder.Gis'
		],

		/**
		 * @cfg {Function}
		 */
		callback: Ext.emptyFn,

		/**
		 * @cfg {Object}
		 */
		scope: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Function} configurationObject.callback
		 * @param {Object} configurationObject.scope
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject); // Apply configuration

			Ext.ns('CMDBuild.configuration');
			CMDBuild.configuration.gis = Ext.create('CMDBuild.model.core.configurations.builder.gis.Gis'); // Setup configuration with defaults

			CMDBuild.proxy.core.configurations.builder.Gis.read({
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					var gisConfigurationObject = decodedResponse[CMDBuild.core.constants.Proxy.DATA] || {};

					if (!Ext.isEmpty(gisConfigurationObject) && !Ext.isEmpty(gisConfigurationObject[CMDBuild.core.constants.Proxy.DATA]))
						gisConfigurationObject = gisConfigurationObject[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.proxy.core.configurations.builder.Gis.readTreeNavigation({
						loadMask: false,
						scope: this.scope || this,
						success: function (response, options, decodedResponse) {
							gisConfigurationObject[CMDBuild.core.constants.Proxy.CARD_BROWSER_BY_DOMAIN_CONFIGURATION] = {
								geoServerLayersMapping: decodedResponse[CMDBuild.core.constants.Proxy.GEO_SERVER_LAYERS_MAPPING],
								root: decodedResponse[CMDBuild.core.constants.Proxy.ROOT]
							};

							CMDBuild.configuration.gis = Ext.create('CMDBuild.model.core.configurations.builder.gis.Gis', gisConfigurationObject); // Configuration model
						},
						callback: this.callback
					});
				}
			});
		}
	});

})();
