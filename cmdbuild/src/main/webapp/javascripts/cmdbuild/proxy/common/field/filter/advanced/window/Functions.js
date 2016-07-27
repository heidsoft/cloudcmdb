(function () {

	Ext.define('CMDBuild.proxy.common.field.filter.advanced.window.Functions', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.common.field.filter.advanced.window.Function'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.FUNCTION, {
				autoLoad: false,
				model: 'CMDBuild.model.common.field.filter.advanced.window.Function',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.functions.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		}
	});

})();
