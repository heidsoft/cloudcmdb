(function () {

	Ext.define('CMDBuild.proxy.domain.Properties', {

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.domain.ClassesStore'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreCardinality: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.NAME, CMDBuild.core.constants.Proxy.VALUE],
				data: [
					['1:1', '1:1'],
					['1:N', '1:N'],
					['N:1', 'N:1'],
					['N:N', 'N:N']
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreClasses: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.domain.ClassesStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters simple, root and system classes
						return (
							record.get(CMDBuild.core.constants.Proxy.NAME) != CMDBuild.core.constants.Global.getRootNameClasses()
							&& record.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) != CMDBuild.core.constants.Global.getTableTypeSimpleTable()
							&& !record.get(CMDBuild.core.constants.Proxy.SYSTEM)
						);
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		}
	});

})();
