(function () {

	Ext.define('CMDBuild.proxy.taskManager.Email', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.taskManager.Grid'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.email.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.TASK_MANAGER, {
				autoLoad: false,
				model: 'CMDBuild.model.taskManager.Grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.taskManager.email.readAll,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TYPE, direction: 'ASC' }
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

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.email.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.email.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.email.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		}
	});

})();
