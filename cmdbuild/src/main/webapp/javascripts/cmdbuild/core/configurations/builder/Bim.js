(function () {

	Ext.define('CMDBuild.core.configurations.builder.Bim', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.core.configurations.builder.Bim'
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
			CMDBuild.configuration.bim = Ext.create('CMDBuild.model.core.configurations.builder.Bim'); // Setup configuration with defaults

			CMDBuild.proxy.core.configurations.builder.Bim.read({
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					var bimConfigurationObject = decodedResponse[CMDBuild.core.constants.Proxy.DATA] || {};

					if (!Ext.isEmpty(bimConfigurationObject) && !Ext.isEmpty(bimConfigurationObject[CMDBuild.core.constants.Proxy.DATA]))
						bimConfigurationObject = bimConfigurationObject[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.proxy.core.configurations.builder.Bim.readRootLayerName({
						loadMask: false,
						scope: this.scope || this,
						success: function (response, options, decodedResponse) {
							bimConfigurationObject[CMDBuild.core.constants.Proxy.ROOT_CLASS] = decodedResponse[CMDBuild.core.constants.Proxy.ROOT];

							CMDBuild.configuration.bim = Ext.create('CMDBuild.model.core.configurations.builder.Bim', bimConfigurationObject); // Configuration model
						},
						callback: this.callback
					});
				}
			});
		}
	});

})();
