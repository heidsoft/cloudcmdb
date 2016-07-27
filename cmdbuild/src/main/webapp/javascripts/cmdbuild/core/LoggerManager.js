(function() {

	Ext.define('CMDBuild.core.LoggerManager', {

		requires: ['Logger.log4javascript'],

		/**
		 * Declares CMDBuild.log object
		 */
		constructor: function() {
			if (!Ext.isEmpty(CMDBuild)) {
				CMDBuild.log = log4javascript.getLogger();
				CMDBuild.log.addAppender(new log4javascript.BrowserConsoleAppender());
			} else {
				_error('CMDBuild object is empty', this);
			}

			// Disable all console messages if IE8 or lower to avoid print window spam
			if (Ext.isIE9m) {
				var console = { log: function() {} };

				log4javascript.setEnabled(false);

				Ext.Error.ignore = true;
			}
		}
	});

	// Convenience methods to debug
		_debug = function() {
			CMDBuild.log.debug.apply(CMDBuild.log, arguments);
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 */
		_deprecated = function(method, classWithError) {
			classWithError = typeof classWithError == 'string' ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(method))
				CMDBuild.log.warn.apply(
					CMDBuild.log,
					Ext.Array.insert(Ext.Array.slice(arguments, 2), 0, ['DEPRECATED (' + classWithError + '): ' + method]) // Slice arguments and prepend custom error message
				);
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 */
		_error = function(message, classWithError) {
			classWithError = Ext.isString(classWithError) ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(message))
				CMDBuild.log.error.apply(
					CMDBuild.log,
					Ext.Array.insert(Ext.Array.slice(arguments, 2), 0, [classWithError + ': ' + message]) // Slice arguments and prepend custom error message
				);
		};

		_msg = function() {
			CMDBuild.log.info.apply(CMDBuild.log, arguments);
		};

		_trace = function() {
			CMDBuild.log.trace(arguments);

			if (console && Ext.isFunction(console.trace))
				console.trace();
		};

		/**
		 * @param {String} message
		 * @param {Mixed} classWithError
		 */
		_warning = function(message, classWithError) {
			classWithError = Ext.isString(classWithError) ? classWithError : Ext.getClassName(classWithError);

			if (!Ext.isEmpty(message))
				CMDBuild.log.warn.apply(
					CMDBuild.log,
					Ext.Array.insert(Ext.Array.slice(arguments, 2), 0, [classWithError + ': ' + message]) // Slice arguments and prepend custom error message
				);
		};

})();