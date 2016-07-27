(function () {

	Ext.define('CMDBuild.core.interfaces.Ajax', {
		extend: 'Ext.data.Connection',

		requires: [
			'CMDBuild.core.CookiesManager',
			'CMDBuild.core.interfaces.messages.Error',
			'CMDBuild.core.interfaces.messages.Warning',
			'CMDBuild.core.interfaces.service.LoadMask',
			'CMDBuild.core.Utils'
		],

		singleton: true,

		/**
		 * Whether a new request should abort any pending requests
		 *
		 * @cfg {Boolean}
		 */
		autoAbort: false,

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

		listeners: {
			beforerequest: function (conn, options, eOpts) {
				Ext.applyIf(options, {
					disableAllMessages: CMDBuild.core.interfaces.Ajax.disableAllMessages || CMDBuild.global.interfaces.Configurations.get('disableAllMessages'),
					disableErrors: CMDBuild.core.interfaces.Ajax.disableErrors || CMDBuild.global.interfaces.Configurations.get('disableErrors'),
					disableWarnings: CMDBuild.core.interfaces.Ajax.disableWarnings || CMDBuild.global.interfaces.Configurations.get('disableWarnings')
				});

				return CMDBuild.core.interfaces.Ajax.trapCallbacks(conn, options);
			}
		},

		/**
		 * Adapter to manage error/warning display and LoadMask
		 *
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Boolean} success
		 * @param {Object} response
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterCallback: function (options, success, response, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);

			// Update authorization cookie expiration date
			CMDBuild.core.CookiesManager.authorizationExpirationUpdate();

			CMDBuild.core.interfaces.service.LoadMask.manage(options.loadMask, false);

			if (!options.disableAllMessages) {
				if (!options.disableWarnings)
					CMDBuild.core.interfaces.messages.Warning.display(decodedResponse);

				if (!options.disableErrors)
					CMDBuild.core.interfaces.messages.Error.display(decodedResponse, options);
			}

			Ext.callback(originalFunction, options.scope, [options, success, response]);
		},

		/**
		 * Adapter to add decodedResponse parameters
		 *
		 * @param {Object} response
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterSuccess: function (response, options, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);

			Ext.callback(originalFunction, options.scope, [response, options, decodedResponse]);
		},

		/**
		 * Adapter to add decodedResponse parameters
		 *
		 * @param {Object} response
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterFailure: function (response, options, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);

			Ext.callback(originalFunction, options.scope, [response, options, decodedResponse]);
		},

		/**
		 * @param {String} jsonResponse
		 *
		 * @returns {Object or String}
		 *
		 * @private
		 */
		decodeJson: function (jsonResponse) {
			jsonResponse = Ext.isEmpty(jsonResponse) ? '{"success":true,"response":null}' : jsonResponse; // Empty response manage

			if (CMDBuild.core.Utils.isJsonString(jsonResponse)) {
				if (!Ext.isEmpty(jsonResponse))
					jsonResponse = jsonResponse.replace(/<\/\w+>$/, '');

				// If throws an error so that wasn't a valid json string
				try {
					return Ext.decode(jsonResponse);
				} catch (e) {
					_error(e, 'CMDBuild.core.interfaces.Ajax');
				}

				return '';
			}

			_error('invalid json string: "' + jsonResponse + '"', 'CMDBuild.core.interfaces.Ajax');

			return '';
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorCallback: function (options) {
			return Ext.bind(CMDBuild.core.interfaces.Ajax.adapterCallback, options.scope, [options.callback], true);
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorFailure: function (options) {
			return Ext.bind(CMDBuild.core.interfaces.Ajax.adapterFailure, options.scope, [options.failure], true);
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorSuccess: function (options) {
			return Ext.bind(CMDBuild.core.interfaces.Ajax.adapterSuccess, options.scope, [options.success], true);
		},

		/**
		 * Overrides to fix a bug that evaluates xhr status to determine success or failure. Uses response success property to determine success status.
		 *
		 * @param {Object} request
		 *
		 * @returns {Object} The response
		 *
		 * @override
		 * @private
		 */
		onComplete: function (request, xdrResult) {
			var options = request.options;
			var success = true; // If response is not correctly formatted will be executed success functions as result
			var response;

			if (request.aborted || request.timedout) {
				response = this.createException(request);
			} else {
				response = this.createResponse(request);
			}

			// Check the response success property to verify real status value
			if (!Ext.isEmpty(response.responseText) && !Ext.isEmpty(Ext.decode(response.responseText).success))
				success = Ext.decode(response.responseText).success;

			if (success) {
				this.fireEvent('requestcomplete', this, response, options);

				Ext.callback(options.success, options.scope, [response, options]);
			} else {
				this.fireEvent('requestexception', this, response, options);

				Ext.callback(options.failure, options.scope, [response, options]);
			}

			Ext.callback(options.callback, options.scope, [options, success, response]);

			delete this.requests[request.id];

			return response;
		},

		/**
		 * Manually builds callback's interceptors to manage loadMask property and callbacks build
		 *
		 * @param {Object} conn
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @returns {Boolean}
		 *
		 * @private
		 */
		trapCallbacks: function (conn, options) {
			CMDBuild.core.interfaces.service.LoadMask.manage(options.loadMask, true);

			Ext.apply(options, {
				callback: CMDBuild.core.interfaces.Ajax.interceptorCallback(options),
				failure: CMDBuild.core.interfaces.Ajax.interceptorFailure(options),
				success: CMDBuild.core.interfaces.Ajax.interceptorSuccess(options)
			});

			return true;
		}
	});

})();
