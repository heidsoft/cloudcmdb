(function () {

	Ext.define('CMDBuild.proxy.dashboard.Chart', {


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
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dashboard.chart.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dashboard.chart.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readForPreview: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dashboard.chart.readForPreview });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dashboard.chart.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dashboard.chart.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters, true);
		}
	});

})();
