(function () {

	Ext.define('CMDBuild.proxy.userAndGroup.user.User', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.userAndGroup.user.DefaultGroup',
			'CMDBuild.model.userAndGroup.user.User',
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

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.user.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		disable: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.user.disable });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.user.User',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.user.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: 'username', direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreDefaultGroup: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.GROUP, {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.user.DefaultGroup',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.user.readAllGroups,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESULT
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

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.user.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.user.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		}
	});

})();
