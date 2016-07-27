(function () {

	/**
	 * To use only inside cache class
	 *
	 * @private
	 */
	Ext.define('CMDBuild.core.cache.Store', {
		extend: 'Ext.data.Store',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.Ajax'
		],

		/**
		 * Parameter to disable all messages display
		 *
		 * @property {Boolean}
		 */
		disableAllMessages: false,

		/**
		 * Parameter to disable only error messages display
		 *
		 * @property {Boolean}
		 */
		disableErrors: false,

		/**
		 * Parameter to disable only warning messages display
		 *
		 * @property {Boolean}
		 */
		disableWarnings: false,

		/**
		 * @cfg {String}
		 */
		groupId: undefined,

		/**
		 * @cfg {String}
		 */
		type: 'store',

		/**
		 * @param {Array} records
		 * @param {Object} operation
		 * @param {Boolean} success
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		callbackInterceptor: function (records, operation, success) {
			var decodedResponse = {};

			if (!Ext.isEmpty(operation) && !Ext.isEmpty(operation.response) && !Ext.isEmpty(operation.response.responseText))
				decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(operation.response.responseText);

			if (!this.disableAllMessages) {
				if (!this.disableWarnings)
					CMDBuild.core.interfaces.messages.Warning.display(decodedResponse);

				if (!this.disableErrors)
					CMDBuild.core.interfaces.messages.Error.display(decodedResponse, operation.request);
			}

			return true;
		},

		/**
		 * @param {Function or Object} options
		 *
		 * @returns {Void}
		 */
		load: function (options) {
			options = Ext.isEmpty(options) ? {} : options;

			Ext.applyIf(options, {
				callback: Ext.emptyFn,
				params: {},
				scope: this
			});

			if (
				CMDBuild.global.Cache.isEnabled()
				&& CMDBuild.global.Cache.isCacheable(this.groupId)
			) {
				var parameters = {
					type: this.type,
					groupId: this.groupId,
					serviceEndpoint: this.proxy.url,
					params: Ext.clone(options.params)
				};

				// Avoid different stores to join results adding store model to parameters
				parameters.params.modelName = this.model.getName();

				if (!CMDBuild.global.Cache.isExpired(parameters)) { // Emulation of success and callback execution
					var cachedValues = CMDBuild.global.Cache.get(parameters);

					this.loadData(cachedValues.records);

					// Interceptor to manage error/warning messages
					options.callback = Ext.Function.createInterceptor(options.callback, this.callbackInterceptor, this);

					return Ext.callback(options.callback, options.scope, [cachedValues.records, cachedValues.operation, cachedValues.success]);
				} else { // Execute real Ajax call
					options.callback = Ext.Function.createSequence(function (records, operation, success) {
						Ext.apply(parameters, {
							values: {
								records: records,
								operation: operation,
								success: success
							}
						});

						// Cache builder call
						CMDBuild.global.Cache.set(parameters);
					}, options.callback);
				}
			}

			// Interceptor to manage error/warning messages
			options.callback = Ext.Function.createInterceptor(options.callback, this.callbackInterceptor, this);

			// Uncachable endpoint manage
			this.callParent(arguments);
		}
	});

})();
