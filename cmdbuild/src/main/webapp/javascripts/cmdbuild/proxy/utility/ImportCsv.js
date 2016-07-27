(function () {

	Ext.define('CMDBuild.proxy.utility.ImportCsv', {

		requires: [
			'CMDBuild.core.configurations.Timeout',
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.model.utility.importCsv.Class',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreClasses: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.utility.importCsv.Class',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function (record) { // Filters root of all classes and processes
						return (
							record.get(CMDBuild.core.constants.Proxy.NAME) != CMDBuild.core.constants.Global.getRootNameClasses()
							&& record.get(CMDBuild.core.constants.Proxy.TYPE) != CMDBuild.core.constants.Global.getTableTypeProcessClass()
						);
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getStoreRecords: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.csv.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CSV, parameters);
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreSeparator: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.VALUE],
				data: [
					[';'],
					[','],
					['|']
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				timeout: CMDBuild.core.configurations.Timeout.getCsvUtility(),
				url: CMDBuild.proxy.index.Json.csv.imports.update
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CSV, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		updateRecords: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.csv.imports.updateRecords });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CSV, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		upload: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.csv.imports.create });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
