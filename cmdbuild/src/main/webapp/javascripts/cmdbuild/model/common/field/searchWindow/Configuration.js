(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.searchWindow.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ENTRY_TYPE, type: 'auto', defaultValue: {} }, // {CMDBuild.cache.CMEntryTypeModel}
			{ name: CMDBuild.core.constants.Proxy.GRID_CONFIGURATION, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY, type: 'boolean', defaultValue: true }
		]
	});

})();
