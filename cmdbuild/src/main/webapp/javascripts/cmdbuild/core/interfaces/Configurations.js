(function () {

	Ext.define('CMDBuild.core.interfaces.Configurations', {

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
		 * @param {String} propertyName
		 *
		 * @returns {Boolean}
		 *
		 * @public
		 */
		get: function (propertyName) {
			if (!Ext.isEmpty(this[propertyName]))
				return this[propertyName];

			return false;
		},

		/**
		 * @param {String} propertyName
		 * @param {Boolean} value
		 *
		 * @returns {Void}
		 *
		 * @public
		 */
		set: function (propertyName, value) {
			if (!Ext.isEmpty(this[propertyName]))
				this[propertyName] = value;
		}
	});

})();
