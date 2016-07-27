(function () {

	Ext.define('CMDBuild.proxy.widget.OpenReport', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.widget.openReport.ReportCombo'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 *
		 * @management
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(),
				url: CMDBuild.proxy.index.Json.report.factory.create
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 *
		 * @management
		 */
		createFactory: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(),
				url: CMDBuild.proxy.index.Json.report.factory.createByTypeCode
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 *
		 * @management
		 */
		download: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.proxy.index.Json.report.factory.print + '?donotdelete=true'  // Add parameter to avoid report delete
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @administration
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.REPORT, {
				autoLoad: true,
				model: 'CMDBuild.model.widget.openReport.ReportCombo',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.report.readByType,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: {
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
		 *
		 * @management
		 */
		getStoreFormats: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[CMDBuild.core.constants.Proxy.PDF, CMDBuild.Translation.pdf],
					[CMDBuild.core.constants.Proxy.CSV, CMDBuild.Translation.csv],
					[CMDBuild.core.constants.Proxy.ODT, CMDBuild.Translation.odt],
					[CMDBuild.core.constants.Proxy.RTF, CMDBuild.Translation.rtf]
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 *
		 * @administration
		 */
		getStoreForceFormat: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[CMDBuild.core.constants.Proxy.PDF, CMDBuild.Translation.pdf],
					[CMDBuild.core.constants.Proxy.CSV, CMDBuild.Translation.csv]
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
		 *
		 * @management
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getReport(),
				url: CMDBuild.proxy.index.Json.report.factory.updateParams
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		}
	});

})();
