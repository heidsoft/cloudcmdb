(function () {

	Ext.define('CMDBuild.proxy.common.entryTypeGrid.filter.advanced.filterEditor.Relations', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DestinationEditorStore',
			'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreDestination: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DestinationEditorStore',
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
				model: 'CMDBuild.model.common.entryTypeGrid.filter.advanced.filterEditor.relations.DomainGrid',
				data: [],

				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DOMAIN_DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAllEntryTypes: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.classes.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CLASS, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAllDomainsByClass: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.domain.readAllByClass });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DOMAIN, parameters);
		}
	});

})();
