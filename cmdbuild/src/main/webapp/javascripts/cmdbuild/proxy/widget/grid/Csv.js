(function () {

	Ext.define('CMDBuild.proxy.grid.Csv', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.LoadMask',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getRecords: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				method: 'GET',
				url: CMDBuild.proxy.index.Json.csv.readAll,
				loadMask: false,
				callback: function (options, success, response) { // Clears server session data
					CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, {
						method: 'GET',
						url: CMDBuild.proxy.index.Json.csv.imports.clearSession,
						loadMask: false
					});

					CMDBuild.core.LoadMask.hide();
				}
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
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
