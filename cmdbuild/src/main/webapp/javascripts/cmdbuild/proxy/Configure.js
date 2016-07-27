(function () {

	Ext.define('CMDBuild.proxy.Configure', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		apply: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getConfigurationSetup(), // Get report timeout from configuration
				url: CMDBuild.proxy.index.Json.configuration.apply
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		dbConnectionCheck: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.configuration.connectionTest });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreDbTypes: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[CMDBuild.core.constants.Proxy.EMPTY, CMDBuild.Translation.empty],
					[CMDBuild.core.constants.Proxy.DEMO, CMDBuild.Translation.demo],
					[CMDBuild.core.constants.Proxy.EXISTING, CMDBuild.Translation.existing]
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreUserType: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					['superuser', CMDBuild.Translation.superUser],
					['limuser', CMDBuild.Translation.limitedUser],
					['new_limuser', CMDBuild.Translation.createLimitedUser]
				]
			});
		}
	});

})();
