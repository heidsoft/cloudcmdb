(function () {

	Ext.define('CMDBuild.proxy.common.field.filter.advanced.window.Relations', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.field.filter.advanced.window.relations.DestinationEditorStore',
			'CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreDestination: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: false,
				model: 'CMDBuild.model.common.field.filter.advanced.window.relations.DestinationEditorStore',
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
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreDomain: function () {
			return Ext.create('Ext.data.ArrayStore', {
				model: 'CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid',
				data: [],

				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DOMAIN, direction: 'ASC' }
				]
			});
		}
	});

})();
