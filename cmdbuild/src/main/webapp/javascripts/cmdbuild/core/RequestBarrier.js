(function () {

	/**
	 * Traffic light class with multiple instances support (id parameter)
	 */
	Ext.define('CMDBuild.core.RequestBarrier', {

		/**
		 * @property {Object}
		 * 	{
		 * 		{Function} callback,
		 * 		{Number} index
		 * 		{Object} scope
		 * 	}
		 *
		 * @private
		 */
		buffer: {},

		/**
		 * @cfg {Boolean}
		 */
		enableCallbackExecution: false,

		/**
		 * @param {Object} parameters
		 * @param {Function} parameters.callback
		 * @param {Number} parameters.executionTimeout
		 * @param {Function} parameters.failure
		 * @param {String} parameters.id
		 * @param {Object} parameters.scope
		 *
		 * @returns {Void}
		 */
		constructor: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& !Ext.isEmpty(parameters.id) && Ext.isString(parameters.id)
				&& !Ext.isEmpty(parameters.callback) && Ext.isFunction(parameters.callback)
			) {
				this.buffer[parameters.id] = {
					callback: parameters.callback,
					index: 0,
					scope: Ext.isEmpty(parameters.scope) ? this : parameters.scope
				};

				// Failure defered function initialization
				if (
					Ext.isNumber(parameters.executionTimeout) && parameters.executionTimeout > 0
					&& !Ext.isEmpty(parameters.failure) && Ext.isFunction(parameters.failure)
				) {
					Ext.defer(function () {
						if (!Ext.Object.isEmpty(this.buffer[parameters.id]))
							Ext.callback(this.buffer[parameters.id].failure, this.buffer[parameters.id].scope);
					}, 5000, this);
				}
			} else {
				_error('invalid initialization parameters', this, parameters);
			}
		},

		/**
		 * @param {String} id
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		callback: function (id) {
			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.Object.isEmpty(this.buffer[id])
			) {
				this.buffer[id].index--;

				this.finalize(id);
			}
		},

		/**
		 * Check callback index and launch last callback but only if enableCallbackExecution parameter is set to true (avoids problems on configurations without delay)
		 * EnableCallbackExecution must be set to true only on last finalize call before barrier setup
		 *
		 * @param {String} id
		 * @param {Boolean} enableCallbackExecution
		 *
		 * @returns {Void}
		 */
		finalize: function (id, enableCallbackExecution) {
			if (!this.enableCallbackExecution) // IMPORTANT: this parameter must not be overridden every call to avoid problems on configurations with delay
				this.enableCallbackExecution = Ext.isBoolean(enableCallbackExecution) ? enableCallbackExecution : false;

			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.Object.isEmpty(this.buffer[id]) && this.buffer[id].index == 0
				&& this.enableCallbackExecution
			) {
				Ext.callback(this.buffer[id].callback, this.buffer[id].scope);

				delete this.buffer[id]; // Buffer reset
			}
		},

		/**
		 * @param {String} id
		 *
		 * @returns {Function}
		 */
		getCallback: function (id) {
			if (
				!Ext.isEmpty(id) && Ext.isString(id)
				&& !Ext.Object.isEmpty(this.buffer[id])
			) {
				this.buffer[id].index++;

				return Ext.bind(this.callback, this, [id]);
			}
		}
	});

})();
