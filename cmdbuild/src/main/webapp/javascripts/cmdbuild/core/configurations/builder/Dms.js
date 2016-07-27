(function () {

	Ext.define('CMDBuild.core.configurations.builder.Dms', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.core.configurations.builder.Dms'
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
		 * @param {Options} configurationObject.scope
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject); // Apply configurations

			Ext.ns('CMDBuild.configuration');
			CMDBuild.configuration.dms = Ext.create('CMDBuild.model.core.configurations.builder.Dms'); // Setup configuration with defaults

			CMDBuild.proxy.core.configurations.builder.Dms.read({
				loadMask: false,
				scope: this.scope || this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.configuration.dms = Ext.create('CMDBuild.model.core.configurations.builder.Dms', decodedResponse); // DMS configuration model
				},
				callback: this.callback
			});
		}
	});

})();
