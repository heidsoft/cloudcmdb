(function () {

	Ext.define('CMDBuild.core.interfaces.Rest', {
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

				return CMDBuild.core.interfaces.Rest.trapCallbacks(conn, options);
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
			var decodedResponse = CMDBuild.core.interfaces.Rest.decodeJson(response.responseText);

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
			var decodedResponse = CMDBuild.core.interfaces.Rest.decodeJson(response.responseText);

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
			var decodedResponse = CMDBuild.core.interfaces.Rest.decodeJson(response.responseText);

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
					_error(e, 'CMDBuild.core.interfaces.Rest');
				}

				return '';
			}

			_error('invalid json string: "' + jsonResponse + '"', 'CMDBuild.core.interfaces.Rest');

			return '';
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorCallback: function (options) {
			return Ext.bind(CMDBuild.core.interfaces.Rest.adapterCallback, options.scope, [options.callback], true);
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorFailure: function (options) {
			return Ext.bind(CMDBuild.core.interfaces.Rest.adapterFailure, options.scope, [options.failure], true);
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorSuccess: function (options) {
			return Ext.bind(CMDBuild.core.interfaces.Rest.adapterSuccess, options.scope, [options.success], true);
		},

		/**
		 * Overrides to evaluate response status
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
			var response = undefined;
			var success = true;

			if (request.aborted || request.timedout) {
				response = this.createException(request);
			} else {
				response = this.createResponse(request);
			}

			if (response.status < 200 || response.status >= 400) { // Manage failures
				success = false;

				var basePath = window.location.toString().split('/');
				basePath = Ext.Array.slice(basePath, 0, basePath.length - 1).join('/');

				response.responseText = Ext.encode({ // Emulate custom exception
					success: false,
					errors: [{
						reason: 'ORM_CUSTOM_EXCEPTION',
						reasonParameters: response.statusText,
						stacktrace: response.request.options.method
						+ ' ' + basePath
						+ '/' + response.request.options.url
						+ ' ' + response.status
						+ ' (' + response.statusText + ')'
					}]
				});
			}

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
				callback: CMDBuild.core.interfaces.Rest.interceptorCallback(options),
				failure: CMDBuild.core.interfaces.Rest.interceptorFailure(options),
				success: CMDBuild.core.interfaces.Rest.interceptorSuccess(options)
			});

			return true;
		}
	});

})();
