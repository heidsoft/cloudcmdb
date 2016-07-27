(function () {

	/**
	 * TODO: waiting for refactor (CRUD)
	 */
	Ext.define('CMDBuild.proxy.common.tabs.attribute.Attribute', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.common.tabs.attribute.Attribute',
			'CMDBuild.model.common.tabs.attribute.Domain',
			'CMDBuild.model.common.tabs.attribute.Type'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.ATTRIBUTE, {
				autoLoad: false,
				model: 'CMDBuild.model.common.tabs.attribute.Attribute',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.attribute.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ATTRIBUTES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.INDEX, direction: 'ASC' },
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreTypes: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.ATTRIBUTE, {
				autoLoad: false,
				model: 'CMDBuild.model.common.tabs.attribute.Type',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.attribute.readTypes,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.TYPES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.VALUE, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreRenceableDomains: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.DOMAIN, {
				autoLoad: false,
				model: 'CMDBuild.model.common.tabs.attribute.Domain',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.attribute.readRenceableDomains,
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
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attribute.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTRIBUTE, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attribute.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTRIBUTE, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.attribute.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTRIBUTE, parameters, true);
		}
	});

})();
