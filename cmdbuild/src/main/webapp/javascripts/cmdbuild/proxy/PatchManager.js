(function () {

	Ext.define('CMDBuild.proxy.PatchManager', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.patchManager.Patch'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: true,
				model: 'CMDBuild.model.patchManager.Patch',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.patchManager.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.PATCHES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				}
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getPatchManager(), // Get patch timeout from configuration
				url: CMDBuild.proxy.index.Json.patchManager.update
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		}
	});

})();
