(function () {

	/**
	 * @management
	 */
	Ext.define('CMDBuild.proxy.report.Report', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.report.Grid'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(),
				url: CMDBuild.proxy.index.Json.report.factory.create
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		download: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.proxy.index.Json.report.factory.print + '?donotdelete=true'  // Add parameter to avoid report delete
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {String} type
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function (type) {
			var extraParams = {};
			extraParams[CMDBuild.core.constants.Proxy.TYPE] = type;

			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.REPORT, {
				autoLoad: false,
				model: 'CMDBuild.model.report.Grid',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.report.readByType,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: extraParams
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
		readTypesTree: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.report.readTypesTree });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(),
				url: CMDBuild.proxy.index.Json.report.factory.updateParams
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.REPORT, parameters, true);
		}
	});

})();
