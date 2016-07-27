(function () {

	Ext.define('CMDBuild.override.data.proxy.Server', {
		override: 'Ext.data.proxy.Server',

		/**
		 * @param {Object} config
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (config) {
			this.callParent(arguments);

			Ext.apply(this, this.extraParams); // To apply extraParams to ProxyServer if defined in store create object [16/01/2015]
		},

		/**
		 * Apply response to operation object of store load callback
		 *
		 * @param {Boolean} success
		 * @param {Ext.data.Operation} operation
		 * @param {Ext.data.Request} request
		 * @param {Object} response
		 * @param {Function} callback
		 * @param {Object} scope
		 *
		 * @returns {Void}
		 */
		processResponse: function (success, operation, request, response, callback, scope) {
			operation.response = response;

			this.callParent(arguments);
		}
	});

})();
