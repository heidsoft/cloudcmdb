(function () {

	Ext.define('CMDBuild.core.configurations.builder.UserInterface', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.core.configurations.builder.UserInterface'
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
			CMDBuild.configuration.userInterface = Ext.create('CMDBuild.model.core.configurations.builder.userInterface.UserInterface'); // Setup configuration with defaults

			CMDBuild.proxy.core.configurations.builder.UserInterface.read({
				loadMask: false,
				scope: this.scope || this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					CMDBuild.configuration.userInterface = Ext.create('CMDBuild.model.core.configurations.builder.userInterface.UserInterface', decodedResponse);
				},
				callback: this.callback
			});
		}
	});

})();
