(function () {

	Ext.define('CMDBuild.bim.proxy.Layer', {

		requires: [
			'CMDBuild.bim.data.CMBimLayerModel',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.BIM, {
				autoLoad: false,
				model: 'CMDBuild.bim.data.CMBimLayerModel',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.bim.layer.read,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.BIM_LAYER
					}
				}
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.layer.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readRootName: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.layer.rootName });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.bim.layer.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters, true);
		}
	});

})();
