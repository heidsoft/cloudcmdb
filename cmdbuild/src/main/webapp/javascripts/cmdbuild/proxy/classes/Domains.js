(function () {

	/**
	 * TODO: waiting for refactor (CRUD)
	 */
	Ext.define('CMDBuild.proxy.classes.Domains', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.classes.tabs.domains.Domain'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.DOMAIN, {
				autoLoad: false,
				model: 'CMDBuild.model.classes.tabs.domains.Domain',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.domain.readAllByClass,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.DOMAINS
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();
