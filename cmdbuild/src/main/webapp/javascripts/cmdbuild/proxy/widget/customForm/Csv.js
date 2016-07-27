(function () {

	Ext.define('CMDBuild.proxy.widget.customForm.Csv', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		decode: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.csv.imports.read });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		exports: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				buildRuntimeForm: true,
				url: CMDBuild.proxy.index.Json.csv.exports.create
			});

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreExportFileFormat: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.NAME],
				data: [
					[CMDBuild.Translation.csv, CMDBuild.core.constants.Proxy.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreImportFileFormat: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.NAME],
				data: [
					[CMDBuild.Translation.csv, CMDBuild.core.constants.Proxy.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Array} excludedValues
		 *
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreImportMode: function (excludedValues) {
			excludedValues = Ext.isArray(excludedValues) ? excludedValues : [];

			var dataValues = [
				[CMDBuild.Translation.replace , 'replace'],
				[CMDBuild.Translation.add, 'add'],
				[CMDBuild.Translation.merge , 'merge']
			];

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.VALUE],
				data: Ext.Array.filter(dataValues, function (valueArray, i, allValueArrays) {
					return !Ext.Array.contains(excludedValues, valueArray[1]);
				}, this),
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
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
		}
	});

})();
