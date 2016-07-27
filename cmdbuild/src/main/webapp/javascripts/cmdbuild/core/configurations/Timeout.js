(function () {

	Ext.define('CMDBuild.core.configurations.Timeout', {

		singleton: true,

		/**
		 * @cfg {Object}
		 *
		 * @private
		 */
		config: {
			base: 90, // (seconds)
			cache: 300000, // 5m (milliseconds)
			configurationSetup: 12000000, // 200m (milliseconds)
			csvUtility: 600000, // 10m (milliseconds)
			gisTreeExpand: 300000, // 5m (milliseconds)
			patchManager: 600000, // 10m (milliseconds)
			report: 7200000, // 2h (milliseconds)
			taskSingleExecution: 7200000, // 2h (milliseconds)
			workflowWidgetsExecutionTimeout: 30000 // 30s (milliseconds)
		},

		/**
		 * @param {Object} config
		 *
		 * @returns {Void}
		 */
		constructor: function (config) {
			this.initConfig(config);
		}
	});

})();
