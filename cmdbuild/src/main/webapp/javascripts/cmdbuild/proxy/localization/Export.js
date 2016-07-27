(function () {

	Ext.define('CMDBuild.proxy.localization.Export', {

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
		exports: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.localization.utility.csv.exports });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreFileFormat: function () {
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
		getStoreEnabledLaguages: function () {
			var data = [];

			Ext.Object.each(CMDBuild.configuration.localization.getEnabledLanguages(), function (tag, languageModel, myself) {
				data.push([tag, languageModel.get(CMDBuild.core.constants.Proxy.DESCRIPTION)]);
			}, this);

			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.TAG, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: data,
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
