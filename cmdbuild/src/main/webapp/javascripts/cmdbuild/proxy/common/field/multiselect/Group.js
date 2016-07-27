(function () {

	Ext.define('CMDBuild.proxy.common.field.multiselect.Group', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.common.field.multiselect.Group'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.GROUP, {
				autoLoad: true,
				model: 'CMDBuild.model.common.field.multiselect.Group',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.group.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.GROUPS
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function (record) { // Filters active groups only
						return record.get(CMDBuild.core.constants.Proxy.IS_ACTIVE);
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();
