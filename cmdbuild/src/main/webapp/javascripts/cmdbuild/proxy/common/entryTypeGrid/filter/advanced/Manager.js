(function () {

	Ext.define('CMDBuild.proxy.common.entryTypeGrid.filter.advanced.Manager', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.filter.group.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreUser: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.FILTER, {
				autoLoad: false,
				model: 'CMDBuild.model.common.entryTypeGrid.filter.advanced.Filter',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.filter.user.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.FILTERS,
						idProperty: CMDBuild.core.constants.Proxy.ID
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' },
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		},

//		/**
//		 * @param {Object} parameters
//		 *
//		 * @returns {Void}
//		 */
//		read: function (parameters) {
//			parameters = Ext.isEmpty(parameters) ? {} : parameters;
//
//			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.filter.group.read });
//
//			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
//		},
//
//		/**
//		 * @param {Object} parameters
//		 *
//		 * @returns {Void}
//		 */
//		readAll: function (parameters) {
//			parameters = Ext.isEmpty(parameters) ? {} : parameters;
//
//			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.filter.groupStore });
//
//			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
//		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.filter.group.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.filter.group.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.FILTER, parameters, true);
		},

//		/**
//		 * Returns a store with the filters for a given group
//		 *
//		 * @param {String} className
//		 *
//		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
//		 */
//		newGroupStore: function (className) {
//			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
//				autoLoad: true,
//				model: 'CMDBuild.model.CMFilterModel',
//				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
//				proxy: {
//					url: CMDBuild.proxy.index.Json.filter.group.readAll,
//					type: 'ajax',
//					reader: {
//						root: 'filters',
//						type: 'json',
//						totalProperty: 'count'
//					},
//					extraParams: {
//						className: className
//					}
//				},
//				sorters: [
//					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
//				]
//			});
//		},
//
//		/**
//		 * Return the store of the current logged user
//		 *
//		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
//		 */
//		newUserStore: function () {
//			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
//				autoLoad: false,
//				model: 'CMDBuild.model.CMFilterModel',
//				proxy: {
//					type: 'ajax',
//					url: CMDBuild.proxy.index.Json.filter.user.readAll,
//					reader: {
//						type: 'json',
//						idProperty: CMDBuild.core.constants.Proxy.ID,
//						root: CMDBuild.core.constants.Proxy.FILTERS
//					}
//				},
//				sorters: [
//					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
//				]
//			 });
//		},
//
//		/**
//		 * @param {String} className
//		 *
//		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
//		 */
//		newSystemStore: function (className) {
//			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
//				autoLoad: true,
//				model: 'CMDBuild.model.CMFilterModel',
//				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
//				proxy: {
//					url: CMDBuild.proxy.index.Json.filter.group.read,
//					type: 'ajax',
//					reader: {
//						type: 'json',
//						root: CMDBuild.core.constants.Proxy.FILTERS,
//						totalProperty: CMDBuild.core.constants.Proxy.COUNT
//					},
//					extraParams: {
//						className: className
//					}
//				},
//				sorters: [
//					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
//				]
//			});
//		},
//
//		/**
//		 * @param {Object} parameters
//		 *
//		 * @returns {Void}
//		 */
//		getDefaults: function (parameters) {
//			parameters = Ext.isEmpty(parameters) ? {} : parameters;
//
//			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.filter.group.defaults.read });
//
//			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
//		},
//
//		/**
//		 * @param {Object} parameters
//		 *
//		 * @returns {Void}
//		 */
//		setDefaults: function (parameters) {
//			parameters = Ext.isEmpty(parameters) ? {} : parameters;
//
//			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.filter.group.defaults.update });
//
//			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
//		}
	});

})();
