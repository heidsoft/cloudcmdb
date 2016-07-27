(function () {

	Ext.define('CMDBuild.core.configurations.builder.Instance', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.core.configurations.builder.Instance'
		],

		/**
		 * @cfg {Function}
		 */
		callback: Ext.emptyFn,

		/**
		 * Enable or disable server calls (set as false within contexts where server calls aren't enabled)
		 *
		 * @cfg {Boolean}
		 */
		enableServerCalls: true,

		/**
		 * @cfg {Object}
		 */
		scope: undefined,

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 * @param {Boolean} configuration.enableServerCalls
		 * @param {Object} configurationObject.scope
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');
			CMDBuild.configuration.instance = Ext.create('CMDBuild.model.core.configurations.builder.Instance'); // Instance configuration model with defaults

			if (this.enableServerCalls) {
				CMDBuild.proxy.core.configurations.builder.Instance.read({
					loadMask: false,
					scope: this.scope || this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

						CMDBuild.configuration.instance = Ext.create('CMDBuild.model.core.configurations.builder.Instance', decodedResponse);
					},
					callback: this.callback
				});
			} else {
				Ext.callback(this.callback, this.scope || this);
			}
		}
	});

})();
