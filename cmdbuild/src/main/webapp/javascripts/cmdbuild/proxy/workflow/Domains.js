(function () {

	Ext.define('CMDBuild.proxy.workflow.Domains', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.workflow.tabs.domains.Grid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.DOMAIN, {
				autoLoad: false,
				model: 'CMDBuild.model.workflow.tabs.domains.Grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.domain.readAllByClass,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.DOMAINS
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();
