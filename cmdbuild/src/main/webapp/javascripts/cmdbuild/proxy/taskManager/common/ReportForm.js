(function () {

	Ext.define('CMDBuild.proxy.taskManager.common.ReportForm', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.constants.Server',
			'CMDBuild.model.taskManager.common.reportForm.Report',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.REPORT, {
				autoLoad: true,
				model: 'CMDBuild.model.taskManager.common.reportForm.Report',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.report.readByType,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					extraParams: {
						// Send maxInteger value to avoid to have a default limit as 20 (sending undefined no values will be recived from server)
						limit: CMDBuild.core.constants.Server.getMaxInteger(),

						type: CMDBuild.core.constants.Proxy.CUSTOM
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreExtension: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.NAME],
				data: [
					[CMDBuild.Translation.csv, CMDBuild.core.constants.Proxy.CSV],
					[CMDBuild.Translation.odt, CMDBuild.core.constants.Proxy.ODT],
					[CMDBuild.Translation.pdf, CMDBuild.core.constants.Proxy.PDF],
					[CMDBuild.Translation.rtf, CMDBuild.core.constants.Proxy.RTF]
				],
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
		readParameters: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(),
				url: CMDBuild.proxy.index.Json.report.factory.create
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		}
	});

})();
