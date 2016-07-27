(function () {

	Ext.define('CMDBuild.proxy.taskManager.TaskManager', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.taskManager.Grid',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.TASK_MANAGER, {
				autoLoad: false,
				model: 'CMDBuild.model.taskManager.Grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.taskManager.readAll,
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
		cyclicExecution: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.cyclicExecution });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		singleExecution: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getTaskSingleExecution(),
				url: CMDBuild.proxy.index.Json.taskManager.singleExecution
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		stop: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.stop });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		}
	});

})();
