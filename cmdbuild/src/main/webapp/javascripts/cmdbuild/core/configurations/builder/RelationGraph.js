(function () {

	Ext.define('CMDBuild.core.configurations.builder.RelationGraph', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.core.configurations.builder.RelationGraph'
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
			Ext.apply(this, configurationObject); // Apply configurations

			Ext.ns('CMDBuild.configuration');
			CMDBuild.configuration.graph = Ext.create('CMDBuild.model.core.configurations.builder.RelationGraph'); // Setup configuration with defaults

			CMDBuild.proxy.core.configurations.builder.RelationGraph.read({
				loadMask: false,
				scope: this.scope || this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.configuration.graph = Ext.create('CMDBuild.model.core.configurations.builder.RelationGraph', decodedResponse); // Configuration model
				},
				callback: this.callback
			});
		}
	});

})();