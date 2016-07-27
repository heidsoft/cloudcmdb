(function() {

	Ext.define('CMDBuild.core.interfaces.FormSubmit', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.CookiesManager',
			'CMDBuild.core.interfaces.messages.Error',
			'CMDBuild.core.interfaces.messages.Warning',
			'CMDBuild.core.interfaces.service.LoadMask'
		],

		singleton: true,

		/**
		 * Adapter to override parameters and manage error/warning display
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options config object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterCallback: function (form, action, originalFunction) {
			// Update authorization cookie expiration date
			CMDBuild.core.CookiesManager.authorizationExpirationUpdate();

			CMDBuild.core.interfaces.service.LoadMask.manage(action.loadMask, false);

			CMDBuild.core.interfaces.messages.Warning.display(action.result);
			CMDBuild.core.interfaces.messages.Error.display(action.result, action);

			Ext.callback(originalFunction, action.scope, [action, action.result.success, action.response]);
		},

		/**
		 * Adapter to override parameters
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterSuccess: function (form, action, originalFunction) {
			Ext.callback(originalFunction, action.scope, [action.response, action, action.result]);
		},

		/**
		 * Adapter to override parameters
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterFailure: function (form, action, originalFunction) {
			Ext.callback(originalFunction, action.scope, [action.response, action, action.result]);
		},

		/**
		 * @param {Object} action - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorCallback: function (action) {
			return Ext.bind(CMDBuild.core.interfaces.FormSubmit.adapterCallback, action.scope, [action.callback], true);
		},

		/**
		 * Builds failure interceptor to create sequence with callback
		 *
		 * @param {Object} action - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorFailure: function (action) {
			return Ext.Function.createSequence(
				Ext.bind(CMDBuild.core.interfaces.FormSubmit.adapterFailure, action.scope, [action.failure], true),
				action.callback,
				action.scope
			);
		},

		/**
		 * Builds success interceptor to create sequence with callback
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		interceptorSuccess: function (action) {
			return Ext.Function.createSequence(
				Ext.bind(CMDBuild.core.interfaces.FormSubmit.adapterSuccess, action.scope, [action.success], true),
				action.callback,
				action.scope
			);
		},

		/**
		 * Manually builds callback's interceptors to manage loadMask property and callbacks build
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options configuration object passed to the request method
		 * @param {Object} eOpts
		 *
		 * @private
		 */
		trapCallbacks: function (form, action, eOpts) {
			// Form with standardSubmit property will execute a normal HTML submit with no success, failure, callback execution
			if (!Ext.isEmpty(action.form) && !action.form.standardSubmit) {
				CMDBuild.core.interfaces.service.LoadMask.manage(action.loadMask, true);

				action.callback = CMDBuild.core.interfaces.FormSubmit.interceptorCallback(action); // First of all because is related to others
				action.failure = CMDBuild.core.interfaces.FormSubmit.interceptorFailure(action);
				action.success = CMDBuild.core.interfaces.FormSubmit.interceptorSuccess(action);
			}
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.method
		 * @param {String} parameters.url
		 * @param {String} parameters.buildRuntimeForm
		 * @param {Ext.form.Basic} parameters.form
		 * @param {Object} parameters.params
		 * @param {Object} parameters.headers
		 * @param {Object} parameters.scope
		 * @param {Function} parameters.callback
		 * @param {Function} parameters.failure
		 * @param {Function} parameters.success
		 *
		 * @public
		 */
		submit: function (parameters) {
			// Set default values
			Ext.applyIf(parameters, {
				method: 'POST',
				buildRuntimeForm: false,
				loadMask: true,
				scope: this,
				callback: Ext.emptyFn,
				failure: Ext.emptyFn,
				success: Ext.emptyFn
			});

			if (!Ext.isEmpty(parameters.form)) { // Submits existing form
				parameters.form.on('beforeaction', CMDBuild.core.interfaces.FormSubmit.trapCallbacks, this, { single: true });
				parameters.form.submit(parameters);
			} else if (Ext.isEmpty(parameters.form) && parameters.buildRuntimeForm) { // Submits a one time builded form
				parameters.params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

				var form = Ext.create('Ext.form.Panel', {
					standardSubmit: true,
					url: parameters.url
				});

				form.submit({
					target: '_blank',
					params: parameters.params
				});

				Ext.defer(function () { // Form cleanup
					form.close();
				}, 100);
			} else{
				_error('form object not managed', 'CMDBuild.core.interfaces.FormSubmit');
			}
		}
	});

})();
