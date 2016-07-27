(function () {

	Ext.define('CMDBuild.proxy.widget.grid.Grid', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * Validates presets function name
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getFunctions: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.proxy.index.Json.functions.readAll
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.FUNCTION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreFromFunction: function (parameters) {
			// Avoid to send limit, page and start parameters in server calls
			parameters.extraParams.limitParam = undefined;
			parameters.extraParams.pageParam = undefined;
			parameters.extraParams.startParam = undefined;

			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: true,
				fields: parameters.fields,
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.functions.readCards,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CARDS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: parameters.extraParams
				}
			});
		}
	});

})();
