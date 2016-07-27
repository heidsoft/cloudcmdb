(function () {

	Ext.define('CMDBuild.proxy.dataView.Sql', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.core.Utils',
			'CMDBuild.model.dataView.sql.GridStore',
			'CMDBuild.model.Function'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dataView.sql.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.DATA_VIEW, {
				autoLoad: false,
				model: 'CMDBuild.model.dataView.sql.GridStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.dataView.sql.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.VIEWS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreDataSources: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.FUNCTION, {
				autoLoad: true,
				model: 'CMDBuild.model.Function',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.functions.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @management
		 */
		getStoreFromSql: function (parameters) {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: true,
				fields: parameters.fields || [],
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				remoteSort: true,
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.card.getSqlCardList,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CARDS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: parameters.extraParams || {}
				}
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dataView.sql.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dataView.sql.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.dataView.sql.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		}
	});

})();
