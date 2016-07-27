(function () {

	Ext.define('CMDBuild.proxy.common.field.comboBox.Language', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.localization.Localization',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.LOCALIZATION, {
				autoLoad: true,
				model: 'CMDBuild.model.localization.Localization',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.utils.readAllAvailableTranslations,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.TRANSLATIONS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();
