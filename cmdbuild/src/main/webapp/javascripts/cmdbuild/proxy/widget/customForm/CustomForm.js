(function () {

	Ext.define('CMDBuild.proxy.widget.customForm.CustomForm', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAllCards: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.card.readAllShort });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CUSTOM_FORM, parameters)
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readFromFunctions: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.functions.readCards });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		}
	});

})();
