(function () {

	Ext.define('CMDBuild.proxy.core.configurations.builder.Localization', {

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
		readAllAvailableTranslations: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.utils.readAllAvailableTranslations });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readDefaultLanguage: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.utils.readDefaultLanguage });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		}
	});

})();
